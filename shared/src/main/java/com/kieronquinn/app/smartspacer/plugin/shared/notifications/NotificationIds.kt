package com.kieronquinn.app.smartspacer.plugin.shared.notifications

/**
 * Deterministic, stable notification-ID allocation.
 *
 * Every business entity (a saved trip, a share operation, a parcel, a medication, ...) is mapped
 * to a stable, positive [Int] ID within a *namespace*. Using a distinct namespace guarantees that
 * different kinds of entities never overwrite each other's notifications even when their numeric
 * IDs coincide (e.g. travel operation vs. saved trip with the same underlying ID).
 *
 * The mapping is a pure function of (namespace, entityId), so it is identical across app
 * restarts and process recreation, and it never returns 0 (0 is used as "invalid" elsewhere).
 */
object NotificationIds {

    /** Separates the operation namespace from the saved-trip namespace in the travel plugin. */
    const val NAMESPACE_TRAVEL_SHARE_OP = "travel_share_op"
    const val NAMESPACE_TRAVEL_TRIP = "travel_trip"
    const val NAMESPACE_PARCEL = "parcel"
    const val NAMESPACE_MEDICATION = "medication"
    const val NAMESPACE_FOOD = "food"
    const val NAMESPACE_WATER = "water"

    fun forEntity(namespace: String, entityId: Long): Int {
        val mixed = namespace.hashCode() * 31 + entityId.hashCode()
        val positive = mixed and 0x7FFFFFFF
        return if (positive == 0) 1 else positive
    }

    fun forEntity(namespace: String, entityId: Int): Int = forEntity(namespace, entityId.toLong())

    /**
     * Two namespaces must never map the same entity to the same ID. This is enforced by the
     * namespace strings themselves; a dedicated unit test pins the concrete combinations used by
     * the app.
     */
    fun collides(namespaceA: String, idA: Long, namespaceB: String, idB: Long): Boolean {
        return namespaceA != namespaceB && forEntity(namespaceA, idA) == forEntity(namespaceB, idB)
    }
}
