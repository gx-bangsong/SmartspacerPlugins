package com.kieronquinn.app.smartspacer.plugin.travel.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TravelShareStateMachineTest {

    private val createdAt = 1_000_000L

    private fun transition(
        from: TravelShareOpState,
        event: TravelShareEvent,
        now: Long = createdAt + 1_000
    ): TravelShareOpState = TravelShareStateMachine.transition(from, event, now, createdAt)

    @Test
    fun `parsing leads to review required after success`() {
        assertEquals(
            TravelShareOpState.REVIEW_REQUIRED,
            transition(TravelShareOpState.PARSING, TravelShareEvent.ParseSucceeded)
        )
    }

    @Test
    fun `parsing leads to failed on parse error`() {
        assertEquals(
            TravelShareOpState.FAILED,
            transition(TravelShareOpState.PARSING, TravelShareEvent.ParseFailed)
        )
    }

    @Test
    fun `review leads to confirmed`() {
        assertEquals(
            TravelShareOpState.CONFIRMED,
            transition(TravelShareOpState.REVIEW_REQUIRED, TravelShareEvent.Confirmed)
        )
    }

    @Test
    fun `any state can be cancelled`() {
        assertEquals(
            TravelShareOpState.CANCELLED,
            transition(TravelShareOpState.PARSING, TravelShareEvent.Cancelled)
        )
        assertEquals(
            TravelShareOpState.CANCELLED,
            transition(TravelShareOpState.REVIEW_REQUIRED, TravelShareEvent.Cancelled)
        )
    }

    @Test
    fun `only review-required can confirm into a persisted trip`() {
        assertTrue(TravelShareStateMachine.canConfirm(TravelShareOpState.REVIEW_REQUIRED))
        assertFalse(TravelShareStateMachine.canConfirm(TravelShareOpState.PARSING))
        assertFalse(TravelShareStateMachine.canConfirm(TravelShareOpState.CONFIRMED))
        assertFalse(TravelShareStateMachine.canConfirm(TravelShareOpState.CANCELLED))
        assertFalse(TravelShareStateMachine.canConfirm(TravelShareOpState.FAILED))
    }

    @Test
    fun `cancelled and failed are terminal without persistence`() {
        assertTrue(TravelShareStateMachine.isTerminalWithoutPersistence(TravelShareOpState.CANCELLED))
        assertTrue(TravelShareStateMachine.isTerminalWithoutPersistence(TravelShareOpState.FAILED))
        assertFalse(TravelShareStateMachine.isTerminalWithoutPersistence(TravelShareOpState.REVIEW_REQUIRED))
        assertFalse(TravelShareStateMachine.isTerminalWithoutPersistence(TravelShareOpState.PARSING))
    }

    @Test
    fun `a review can never confirm after the draft expired`() {
        val expiredNow = createdAt + TravelShareDraft.DRAFT_TTL_MS + 1
        assertEquals(
            TravelShareOpState.CANCELLED,
            transition(TravelShareOpState.REVIEW_REQUIRED, TravelShareEvent.Confirmed, expiredNow)
        )
    }

    @Test
    fun `expired drafts are treated as cancelled for any event`() {
        val expiredNow = createdAt + TravelShareDraft.DRAFT_TTL_MS + 1
        assertEquals(
            TravelShareOpState.CANCELLED,
            transition(TravelShareOpState.PARSING, TravelShareEvent.ParseSucceeded, expiredNow)
        )
    }

    @Test
    fun `draft expiry check respects the ttl`() {
        val draft = TravelShareDraft(
            opId = "op",
            state = TravelShareOpState.REVIEW_REQUIRED,
            textHash = null,
            trainNumber = "G1",
            departureStation = "A",
            arrivalStation = "B",
            departureTime = 0L,
            seat = null,
            createdAt = createdAt,
            updatedAt = createdAt
        )
        assertFalse(draft.isExpired(createdAt + TravelShareDraft.DRAFT_TTL_MS - 1))
        assertTrue(draft.isExpired(createdAt + TravelShareDraft.DRAFT_TTL_MS))
        assertTrue(draft.isExpired(createdAt + TravelShareDraft.DRAFT_TTL_MS + 1))
    }
}
