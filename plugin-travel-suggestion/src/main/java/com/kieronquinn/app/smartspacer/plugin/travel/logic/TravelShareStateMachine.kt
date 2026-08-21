package com.kieronquinn.app.smartspacer.plugin.travel.logic

/**
 * Minimal, privacy-preserving snapshot of a parsed trip used by the share flow.
 *
 * Deliberately does NOT carry the passenger name or the raw SMS text: the share-flow draft that
 * survives process death / lives in notification actions must only contain the fields needed to
 * review and confirm the trip. The raw text is only represented by a non-reversible hash so
 * duplicate share intents can be detected without storing the content.
 */
data class TravelShareDraft(
    val opId: String,
    val state: TravelShareOpState,
    val textHash: String?,
    val trainNumber: String,
    val departureStation: String,
    val arrivalStation: String?,
    val departureTime: Long,
    val seat: String?,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun withState(newState: TravelShareOpState, now: Long): TravelShareDraft =
        copy(state = newState, updatedAt = now)

    fun isExpired(now: Long): Boolean = now > createdAt + DRAFT_TTL_MS

    companion object {
        /** Share drafts are pruned 30 minutes after they were created. */
        const val DRAFT_TTL_MS = 30L * 60 * 1000L
    }
}

enum class TravelShareOpState {
    PARSING,
    REVIEW_REQUIRED,
    CONFIRMED,
    CANCELLED,
    FAILED
}

/**
 * Pure state machine for the "share → parse → review → confirm" flow.
 *
 * Guarantees:
 *  - a trip is only persisted when the flow reaches [TravelShareOpState.CONFIRMED];
 *  - [TravelShareOpState.CANCELLED]/[TravelShareOpState.FAILED] never write to the database;
 *  - an expired draft is treated as cancelled (so stale notification actions cannot resurrect an
 *    operation and double-insert a trip).
 */
object TravelShareStateMachine {

    fun initialState(now: Long): TravelShareOpState = TravelShareOpState.PARSING

    fun transition(
        state: TravelShareOpState,
        event: TravelShareEvent,
        now: Long,
        createdAt: Long
    ): TravelShareOpState {
        if (now > createdAt + TravelShareDraft.DRAFT_TTL_MS) {
            return TravelShareOpState.CANCELLED
        }
        return when (event) {
            TravelShareEvent.ParseSucceeded -> TravelShareOpState.REVIEW_REQUIRED
            TravelShareEvent.ParseFailed -> TravelShareOpState.FAILED
            TravelShareEvent.ReviewShown -> {
                if (state == TravelShareOpState.PARSING) TravelShareOpState.REVIEW_REQUIRED else state
            }
            TravelShareEvent.Confirmed -> TravelShareOpState.CONFIRMED
            TravelShareEvent.Cancelled -> TravelShareOpState.CANCELLED
        }
    }

    /** True when the flow is allowed to write the trip to the database. */
    fun canConfirm(state: TravelShareOpState): Boolean = state == TravelShareOpState.REVIEW_REQUIRED

    /** States that must never write to the database. */
    fun isTerminalWithoutPersistence(state: TravelShareOpState): Boolean =
        state == TravelShareOpState.CANCELLED || state == TravelShareOpState.FAILED
}

sealed class TravelShareEvent {
    object ParseSucceeded : TravelShareEvent()
    object ParseFailed : TravelShareEvent()
    object ReviewShown : TravelShareEvent()
    object Confirmed : TravelShareEvent()
    object Cancelled : TravelShareEvent()
}
