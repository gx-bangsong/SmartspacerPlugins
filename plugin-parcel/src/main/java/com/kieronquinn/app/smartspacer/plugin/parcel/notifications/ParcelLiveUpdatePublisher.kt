package com.kieronquinn.app.smartspacer.plugin.parcel.notifications

import com.kieronquinn.app.smartspacer.plugin.parcel.data.ParcelDao
import com.kieronquinn.app.smartspacer.plugin.parcel.repositories.SettingsRepository
import com.kieronquinn.app.smartspacer.plugin.parcel.repositories.getBlocking

/**
 * Re-posts pending pickup codes as Live Updates. Needed because turning the experimental
 * switch on, scanning the inbox, or refreshing Smartspacer does not otherwise emit a new SMS.
 */
class ParcelLiveUpdatePublisher(
    private val parcelDao: ParcelDao,
    private val settingsRepository: SettingsRepository,
    private val notificationController: ParcelNotificationController,
    private val suppressionRepository: ParcelSuppressionRepository
) {
    suspend fun publishPending() {
        val promoted = settingsRepository.promotedLiveUpdates.getBlocking()
        if (!promoted) return
        for (parcel in parcelDao.getPendingParcelsList()) {
            if (!suppressionRepository.isSuppressed(parcel.id)) {
                notificationController.postParcel(parcel, promoted = true)
            }
        }
    }
}
