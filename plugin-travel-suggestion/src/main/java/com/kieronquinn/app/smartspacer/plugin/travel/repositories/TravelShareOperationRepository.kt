package com.kieronquinn.app.smartspacer.plugin.travel.repositories

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kieronquinn.app.smartspacer.plugin.travel.logic.TravelShareDraft
import com.kieronquinn.app.smartspacer.plugin.travel.logic.TravelShareOpState

/**
 * Persists the minimal share-flow draft so notification actions and process recreation can
 * resume an operation. Only non-sensitive fields are stored (see [TravelShareDraft]); the raw
 * share text is never written and drafts are pruned after their TTL.
 */
class TravelShareOperationRepository(context: Context) {

    companion object {
        private const val PREFS_NAME = "travel_share_ops"
        private const val KEY_DRAFT_PREFIX = "draft_"
        private const val KEY_OPID_INDEX = "opid_by_hash_"
        private val gson = Gson()
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val draftType = object : TypeToken<TravelShareDraft>() {}.type

    fun save(draft: TravelShareDraft) {
        prefs.edit()
            .putString(KEY_DRAFT_PREFIX + draft.opId, gson.toJson(draft, draftType))
            .apply()
        draft.textHash?.let { hash ->
            prefs.edit().putString(KEY_OPID_INDEX + hash, draft.opId).apply()
        }
    }

    fun get(opId: String): TravelShareDraft? {
        val json = prefs.getString(KEY_DRAFT_PREFIX + opId, null) ?: return null
        return try {
            gson.fromJson(json, draftType)
        } catch (e: Exception) {
            null
        }
    }

    /** Finds an in-flight operation that was created from the same share text (by fingerprint). */
    fun findByTextHash(textHash: String?): TravelShareDraft? {
        if (textHash.isNullOrBlank()) return null
        val opId = prefs.getString(KEY_OPID_INDEX + textHash, null) ?: return null
        return get(opId)
    }

    fun delete(opId: String) {
        get(opId)?.textHash?.let { hash ->
            prefs.edit().remove(KEY_OPID_INDEX + hash).apply()
        }
        prefs.edit().remove(KEY_DRAFT_PREFIX + opId).apply()
    }

    /** Removes expired drafts; called on app start and before resuming an operation. */
    fun pruneExpired(now: Long) {
        val expired = allDrafts().filter { it.isExpired(now) }
        expired.forEach { delete(it.opId) }
    }

    private fun allDrafts(): List<TravelShareDraft> {
        return prefs.all.entries
            .filter { it.key.startsWith(KEY_DRAFT_PREFIX) }
            .mapNotNull { (_, value) ->
                try {
                    gson.fromJson(value as? String, draftType)
                } catch (e: Exception) {
                    null
                }
            }
    }

    /** Marks every in-flight operation cancelled (used when the notification is dismissed). */
    fun cancelAllInFlight(now: Long) {
        allDrafts().forEach { draft ->
            if (draft.state == TravelShareOpState.PARSING || draft.state == TravelShareOpState.REVIEW_REQUIRED) {
                save(draft.withState(TravelShareOpState.CANCELLED, now))
            }
        }
    }
}
