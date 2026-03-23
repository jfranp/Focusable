package com.example.focusable.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusSessionDao {

    @Insert
    suspend fun insert(session: FocusSessionEntity): Long

    @Update
    suspend fun update(session: FocusSessionEntity)

    @Query("SELECT * FROM focus_sessions WHERE endTime IS NULL ORDER BY startTime DESC LIMIT 1")
    fun observeActiveSession(): Flow<FocusSessionEntity?>

    @Query("SELECT * FROM focus_sessions WHERE endTime IS NOT NULL ORDER BY startTime DESC")
    fun observePastSessions(): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE id = :id")
    suspend fun getById(id: Long): FocusSessionEntity?

    @Query("SELECT * FROM focus_sessions WHERE endTime IS NULL ORDER BY startTime DESC LIMIT 1")
    suspend fun getActiveSession(): FocusSessionEntity?
}
