package com.kieronquinn.app.smartspacer.plugin.parcel.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ParcelDao {
    @Query("SELECT * FROM parcels WHERE status = 'PENDING' ORDER BY timestamp DESC")
    fun getPendingParcels(): Flow<List<ParcelItem>>

    @Query("SELECT * FROM parcels WHERE status = 'PENDING' ORDER BY timestamp DESC")
    suspend fun getPendingParcelsList(): List<ParcelItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParcel(parcel: ParcelItem): Long

    @Update
    suspend fun updateParcel(parcel: ParcelItem)

    @Query("UPDATE parcels SET status = 'PICKED_UP' WHERE id = :id")
    suspend fun markAsPickedUp(id: Long)

    @Query("UPDATE parcels SET status = 'EXPIRED' WHERE timestamp < :threshold AND status = 'PENDING'")
    suspend fun markOldParcelsAsExpired(threshold: Long)

    @Query("DELETE FROM parcels WHERE timestamp < :threshold AND (status = 'PICKED_UP' OR status = 'EXPIRED')")
    suspend fun deleteOldParcels(threshold: Long)

    @Query("SELECT * FROM parcels WHERE rawText = :rawText LIMIT 1")
    suspend fun getParcelByRawText(rawText: String): ParcelItem?

    @Query("SELECT * FROM parcels WHERE pickupCode = :pickupCode AND stationName = :stationName AND status = 'PENDING' LIMIT 1")
    suspend fun findDuplicate(pickupCode: String, stationName: String?): ParcelItem?
}
