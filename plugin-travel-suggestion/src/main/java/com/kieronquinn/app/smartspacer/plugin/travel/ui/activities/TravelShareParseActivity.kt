package com.kieronquinn.app.smartspacer.plugin.travel.ui.activities

import android.content.ClipData
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.kieronquinn.app.smartspacer.plugin.travel.R
import com.kieronquinn.app.smartspacer.plugin.travel.data.TravelInfoDao
import com.kieronquinn.app.smartspacer.plugin.travel.data.TravelInfoItem
import com.kieronquinn.app.smartspacer.plugin.travel.data.TravelTripSave
import com.kieronquinn.app.smartspacer.plugin.travel.logic.ShareTextResult
import com.kieronquinn.app.smartspacer.plugin.travel.logic.TravelDedupe
import com.kieronquinn.app.smartspacer.plugin.travel.logic.TravelShareDraft
import com.kieronquinn.app.smartspacer.plugin.travel.logic.TravelShareEvent
import com.kieronquinn.app.smartspacer.plugin.travel.logic.TravelShareOpState
import com.kieronquinn.app.smartspacer.plugin.travel.logic.TravelShareStateMachine
import com.kieronquinn.app.smartspacer.plugin.travel.logic.TravelShareTextExtractor
import com.kieronquinn.app.smartspacer.plugin.travel.logic.TripKey
import com.kieronquinn.app.smartspacer.plugin.travel.notifications.TravelNotificationController
import com.kieronquinn.app.smartspacer.plugin.travel.providers.TravelTargetProvider
import com.kieronquinn.app.smartspacer.plugin.travel.repositories.TravelScheduler
import com.kieronquinn.app.smartspacer.plugin.travel.repositories.TravelShareOperationRepository
import com.kieronquinn.app.smartspacer.plugin.travel.repositories.TravelSettingsRepository
import com.kieronquinn.app.smartspacer.plugin.travel.repositories.TravelSuppressionRepository
import com.kieronquinn.app.smartspacer.plugin.travel.ui.fragments.ManualPasteFragment
import com.kieronquinn.app.smartspacer.plugin.travel.ui.fragments.ShareFlowContent
import com.kieronquinn.app.smartspacer.plugin.travel.ui.fragments.TravelTheme
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import com.kieronquinn.app.smartspacer.shared.smsparser.ParseResultStatus
import com.kieronquinn.app.smartspacer.shared.smsparser.SmsParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.security.MessageDigest
import java.util.UUID

/**
 * Share receiver activity for the "解析出行信息" (parse travel info) entry point in the Android
 * sharesheet.
 *
 * Flow (each share gets a stable operation UUID and its own notification ID namespace):
 *
 *  PARSING → post promoted Live Update (indeterminate progress, "取消解析" action)
 *         → parse on a background dispatcher (never the main thread)
 *  REVIEW_REQUIRED → in-place update of the same notification + editable review dialog
 *                  → "核对并保存" / "取消" actions
 *  CONFIRMED → only now write Room + notify Smartspacer + schedule the departure reminder;
 *            → the operation notification is cancelled and a brief "已保存" notification is shown
 *  CANCELLED → no database write, notification cancelled
 *  FAILED → ongoing state ended, dismissable error notification, manual paste fallback offered
 *
 * Design notes:
 *  - Reuses the existing [SmsParser] and the existing [ManualPasteFragment] review UI; no
 *    second parser or field mapping is introduced.
 *  - The raw share text is never logged, never stored, and never persisted — only a SHA-256
 *    fingerprint is kept in the temporary draft so duplicate share intents can be detected.
 *  - The persisted draft holds only the minimal non-sensitive fields (no passenger name).
 *  - Rotation / process recreation / notification-action re-entry are deterministic: the
 *    operation is keyed by its UUID, dedupe guards the database, and drafts expire after 30 min.
 */
class TravelShareParseActivity : FragmentActivity() {

    companion object {
        const val EXTRA_OP_ID = "extra_op_id"
        const val EXTRA_OP_ACTION = "extra_op_action"
        const val ACTION_REVIEW_OP = "com.kieronquinn.app.smartspacer.plugin.travel.ACTION_REVIEW_OP"
        const val ACTION_CANCEL_OP = "com.kieronquinn.app.smartspacer.plugin.travel.ACTION_CANCEL_OP"
        private const val TAG_REVIEW = "travel_share_review"
        private const val STATE_OP_ID = "state_op_id"
    }

    private val travelInfoDao by inject<TravelInfoDao>()
    private val travelScheduler by inject<TravelScheduler>()
    private val settingsRepository by inject<TravelSettingsRepository>()
    private val opRepository by inject<TravelShareOperationRepository>()
    private val suppressionRepository by inject<TravelSuppressionRepository>()
    private val notificationController by inject<TravelNotificationController>()

    private var opId: String? = null
    private var opState: TravelShareOpState? = null

    private val backCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (opState == TravelShareOpState.PARSING) {
                cancelOperation()
            } else {
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, backCallback)
        opRepository.pruneExpired(System.currentTimeMillis())

        savedInstanceState?.getString(STATE_OP_ID)?.let { restored ->
            opId = restored
            loadDraft()
        }

        setContentView(
            ComposeView(this).apply {
                setContent {
                    TravelTheme {
                        ShareFlowContent(
                            state = opState,
                            onManualPaste = { showReviewFragment(initialInfo = null) },
                            onClose = { finish() }
                        )
                    }
                }
            }
        )

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        opId?.let { outState.putString(STATE_OP_ID, it) }
    }

    override fun onStart() {
        super.onStart()
        // Re-attach callbacks to a review fragment that survived a configuration change.
        val fragment = supportFragmentManager.findFragmentByTag(TAG_REVIEW) as? ManualPasteFragment
        fragment?.let { attachReviewListeners(it) }
    }

    // ------------------------------------------------------------------ intent handling

    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SEND -> startShareOperation(intent)
            ACTION_CANCEL_OP -> handleCancelAction(intent)
            ACTION_REVIEW_OP -> handleReviewAction(intent)
            else -> {
                // Malformed / unrelated launch: nothing to do, never crash.
                if (opId == null) finish()
            }
        }
    }

    private fun handleCancelAction(intent: Intent) {
        val id = intent.getStringExtra(EXTRA_OP_ID)
        if (id != null) {
            opId = id
            loadDraft()
            cancelOperation()
        } else {
            finish()
        }
    }

    private fun handleReviewAction(intent: Intent) {
        val id = intent.getStringExtra(EXTRA_OP_ID) ?: opId
        if (id == null) {
            finish()
            return
        }
        opId = id
        val draft = loadDraft()
        when (draft?.state) {
            // Resume the review with the stored (non-sensitive) fields pre-filled.
            TravelShareOpState.REVIEW_REQUIRED -> showReviewFragment(initialInfo = draft)
            TravelShareOpState.FAILED -> showReviewFragment(initialInfo = null)
            TravelShareOpState.PARSING -> {
                // After process death the parse coroutine is gone and the raw text is (by design)
                // not persisted — fall back to the manual paste flow so the user can still save.
                val now = System.currentTimeMillis()
                val nextState = TravelShareStateMachine.transition(
                    draft.state, TravelShareEvent.ParseFailed, now, draft.createdAt
                )
                saveDraft(draft.withState(nextState, now))
                notificationController.postShareFailed(draft.opId)
                showReviewFragment(initialInfo = null)
            }
            TravelShareOpState.CONFIRMED -> {
                Toast.makeText(this, R.string.share_op_already_saved, Toast.LENGTH_SHORT).show()
                finish()
            }
            else -> finish()
        }
    }

    private fun startShareOperation(intent: Intent) {
        val existingOpId = opId
        val now = System.currentTimeMillis()

        val extraText = intent.getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString()
        val clipText = intent.clipData?.let(::clipTextFrom)

        when (val result = TravelShareTextExtractor.extract(
            action = intent.action,
            mimeType = intent.type,
            extraText = extraText,
            clipText = clipText
        )) {
            is ShareTextResult.Success -> {
                val text = result.text
                val hash = sha256(text)

                // Duplicate share intent for the same in-flight operation: resume it instead of
                // starting a second one, so the same operation cannot double-insert a trip.
                val inFlight = opRepository.findByTextHash(hash)
                if (inFlight != null && !inFlight.isExpired(now) &&
                    (inFlight.state == TravelShareOpState.PARSING ||
                        inFlight.state == TravelShareOpState.REVIEW_REQUIRED)
                ) {
                    opId = inFlight.opId
                    loadDraft()
                    if (inFlight.state == TravelShareOpState.REVIEW_REQUIRED) {
                        showReviewFragment(initialInfo = inFlight)
                    } else {
                        // PARSING resumed after rotation/process recreation: the previous parse
                        // coroutine died with the old activity, so re-run it (idempotent — the
                        // transition only happens while the op is still PARSING).
                        notificationController.postShareParsing(inFlight.opId)
                        parseAsync(text)
                    }
                    return
                }

                val opUuid = existingOpId ?: UUID.randomUUID().toString()
                opId = opUuid
                opState = TravelShareOpState.PARSING
                saveDraft(
                    TravelShareDraft(
                        opId = opUuid,
                        state = TravelShareOpState.PARSING,
                        textHash = hash,
                        trainNumber = "",
                        departureStation = "",
                        arrivalStation = null,
                        departureTime = 0L,
                        seat = null,
                        createdAt = now,
                        updatedAt = now
                    )
                )
                notificationController.postShareParsing(opUuid)
                parseAsync(text)
            }
            ShareTextResult.Empty, ShareTextResult.WrongMimeType, ShareTextResult.TooLong -> {
                val opUuid = existingOpId ?: UUID.randomUUID().toString()
                opId = opUuid
                opState = TravelShareOpState.FAILED
                saveDraft(
                    TravelShareDraft(
                        opId = opUuid,
                        state = TravelShareOpState.FAILED,
                        textHash = null,
                        trainNumber = "",
                        departureStation = "",
                        arrivalStation = null,
                        departureTime = 0L,
                        seat = null,
                        createdAt = now,
                        updatedAt = now
                    )
                )
                notificationController.postShareFailed(opUuid)
            }
        }
    }

    private fun clipTextFrom(clip: ClipData): String? {
        return try {
            if (clip.itemCount > 0) clip.getItemAt(0).coerceToText(this)?.toString() else null
        } catch (e: Exception) {
            null
        }
    }

    // ------------------------------------------------------------------ parsing

    private fun parseAsync(text: String) {
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    val customRulesJson = settingsRepository.customRulesJson.first()
                    val parser = customRulesJson?.let { SmsParser(it) } ?: SmsParser(this@TravelShareParseActivity)
                    parser.parseTravelInfo(text)
                } catch (e: Exception) {
                    null
                }
            }
            val current = loadDraft() ?: return@launch
            if (current.state != TravelShareOpState.PARSING) return@launch // cancelled meanwhile

            if (result != null && result.status == ParseResultStatus.SUCCESS && result.travelInfo != null) {
                val info = result.travelInfo!!
                val now = System.currentTimeMillis()
                val parsed = current.copy(
                    trainNumber = info.trainNumber,
                    departureStation = info.departureStation,
                    arrivalStation = info.arrivalStation,
                    departureTime = info.departureTime,
                    seat = info.seat
                )
                val nextState = TravelShareStateMachine.transition(
                    parsed.state, TravelShareEvent.ParseSucceeded, now, parsed.createdAt
                )
                saveDraft(parsed.withState(nextState, now))
                notificationController.postShareReview(parsed)
                showReviewFragment(initialInfo = parsed)
            } else {
                val now = System.currentTimeMillis()
                val nextState = TravelShareStateMachine.transition(
                    current.state, TravelShareEvent.ParseFailed, now, current.createdAt
                )
                saveDraft(current.withState(nextState, now))
                notificationController.postShareFailed(current.opId)
            }
        }
    }

    // ------------------------------------------------------------------ review UI

    private fun showReviewFragment(initialInfo: TravelShareDraft?) {
        val existing = supportFragmentManager.findFragmentByTag(TAG_REVIEW)
        if (existing != null) return

        val fragment = ManualPasteFragment().apply {
            setInitialTravelInfo(initialInfo)
            setSource("share")
        }
        attachReviewListeners(fragment)
        fragment.show(supportFragmentManager, TAG_REVIEW)
    }

    private fun attachReviewListeners(fragment: ManualPasteFragment) {
        fragment.setOnTravelItemAddedListener { item ->
            confirmTrip(item)
        }
        fragment.setOnCancelListener {
            cancelOperation()
        }
    }

    private fun confirmTrip(item: TravelInfoItem) {
        val id = opId ?: return
        val draft = loadDraft() ?: return
        val now = System.currentTimeMillis()

        lifecycleScope.launch {
            // Only a REVIEW_REQUIRED operation may persist; a stale/expired op never writes.
            if (!TravelShareStateMachine.canConfirm(draft.state) || draft.isExpired(now)) {
                cancelOperation()
                return@launch
            }

            val existing = travelInfoDao.getUnusedTrips(now)
            val duplicate = TravelDedupe.isDuplicate(
                existing.map { TripKey(it.trainNumber, it.departureTime) },
                item.trainNumber,
                item.departureTime
            )
            if (duplicate) {
                // Same trip already stored (duplicate SMS/share): never double-insert.
                opRepository.delete(id)
                notificationController.cancelShareOp(id)
                Toast.makeText(this@TravelShareParseActivity, R.string.share_op_duplicate, Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }

            val itemWithSource = item.copy(source = "share")
            val savedItem = TravelTripSave.afterInsert(itemWithSource, travelInfoDao.insert(itemWithSource))
            travelScheduler.scheduleReminder(savedItem)
            suppressionRepository.clearForTrip(savedItem.id)
            SmartspacerTargetProvider.notifyChange(this@TravelShareParseActivity, TravelTargetProvider::class.java)

            val confirmed = TravelShareStateMachine.transition(
                draft.state, TravelShareEvent.Confirmed, now, draft.createdAt
            )
            saveDraft(draft.withState(confirmed, now))
            notificationController.cancelShareOp(id)
            notificationController.postShareSaved(shortTripSummary(savedItem))
            finish()
        }
    }

    private fun cancelOperation() {
        val id = opId ?: run {
            finish()
            return
        }
        opRepository.delete(id)
        notificationController.cancelShareOp(id)
        opId = null
        opState = null
        finish()
    }

    // ------------------------------------------------------------------ helpers

    private fun loadDraft(): TravelShareDraft? {
        val id = opId ?: return null
        val draft = opRepository.get(id) ?: return null
        opState = draft.state
        return draft
    }

    private fun saveDraft(draft: TravelShareDraft) {
        opRepository.save(draft)
        opState = draft.state
    }

    private fun shortTripSummary(item: TravelInfoItem): String {
        val sb = StringBuilder(item.trainNumber).append(" · ").append(item.departureStation)
        if (!item.arrivalStation.isNullOrBlank()) sb.append(" → ").append(item.arrivalStation)
        return sb.toString()
    }

    private fun sha256(text: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(text.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }
}
