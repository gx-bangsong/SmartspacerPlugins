package com.kieronquinn.app.smartspacer.plugin.shared.permissions

/**
 * Degradation contract when the user refuses a capability. These gates are deliberately
 * conservative: refusing notifications or SMS must never block database writes, Smartspacer
 * targets, share-sheet parsing or manual paste.
 */
object PluginFeatureGates {

    fun canAutoParseSms(smsReceiveGranted: Boolean, smsReadGranted: Boolean): Boolean {
        return smsReceiveGranted && smsReadGranted
    }

    fun canUseShareOrManualPaste(): Boolean = true

    fun canPersistAndShowSmartspacerTarget(): Boolean = true

    fun canPostNotification(notificationsGranted: Boolean): Boolean = notificationsGranted

    fun canPostPromotedLiveUpdate(
        atLeastBaklavaQpr1: Boolean,
        canPostPromotedNotifications: Boolean,
        notificationsGranted: Boolean
    ): Boolean {
        return notificationsGranted && atLeastBaklavaQpr1 && canPostPromotedNotifications
    }

    fun shouldFallBackToNormalNotification(
        atLeastBaklavaQpr1: Boolean,
        canPostPromotedNotifications: Boolean
    ): Boolean {
        return !atLeastBaklavaQpr1 || !canPostPromotedNotifications
    }
}
