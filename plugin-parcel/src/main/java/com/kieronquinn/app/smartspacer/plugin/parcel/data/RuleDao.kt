package com.kieronquinn.app.smartspacer.plugin.parcel.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RuleDao {
    @Query("SELECT * FROM rules ORDER BY priority DESC")
    fun getAllRules(): Flow<List<RuleItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: RuleItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRules(rules: List<RuleItem>)

    @Delete
    suspend fun deleteRule(rule: RuleItem)
}
