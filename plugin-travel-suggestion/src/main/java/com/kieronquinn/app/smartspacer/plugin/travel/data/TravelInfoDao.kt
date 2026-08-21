package com.kieronquinn.app.smartspacer.plugin.travel.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TravelInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TravelInfoItem)

    @Update
    suspend fun update(item: TravelInfoItem)

    @Delete
    suspend fun delete(item: TravelInfoItem)

    @Query("SELECT * FROM travel_info_items ORDER BY departureTime ASC")
    fun getAllFlow(): Flow<List<TravelInfoItem>>

    @Query("SELECT * FROM travel_info_items WHERE id = :id")
    suspend fun getById(id: Int): TravelInfoItem?

    @Query("SELECT * FROM travel_info_items WHERE isUsed = 0 AND departureTime > :now ORDER BY departureTime ASC")
    suspend fun getUnusedTrips(now: Long): List<TravelInfoItem>

    @Query("SELECT * FROM travel_info_items WHERE isUsed = 0 AND departureTime > :now ORDER BY departureTime ASC")
    fun getUnusedTripsFlow(now: Long): Flow<List<TravelInfoItem>>

    @Query("SELECT * FROM travel_info_items WHERE isUsed = 0")
    suspend fun getUnusedAll(): List<TravelInfoItem>
}
