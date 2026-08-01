package com.kieronquinn.app.smartspacer.plugin.checkin.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckInDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CheckInItem)

    @Update
    suspend fun update(item: CheckInItem)

    @Delete
    suspend fun delete(item: CheckInItem)

    @Query("SELECT * FROM check_in_items ORDER BY date DESC")
    fun getAllFlow(): Flow<List<CheckInItem>>

    @Query("SELECT * FROM check_in_items WHERE date = :date")
    suspend fun getByDate(date: String): CheckInItem?

    @Query("SELECT * FROM check_in_items WHERE id = :id")
    suspend fun getById(id: Int): CheckInItem?
}
