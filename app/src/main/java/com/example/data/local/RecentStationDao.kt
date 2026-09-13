package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentStationDao {
    @Query("SELECT * FROM recent_stations ORDER BY playedAt DESC LIMIT 5")
    fun getRecentStations(): Flow<List<RecentStationEntity>>

    @Query("SELECT * FROM recent_stations ORDER BY playedAt DESC LIMIT 5")
    suspend fun getRecentStationsList(): List<RecentStationEntity>

    @Query("SELECT COUNT(*) FROM recent_stations")
    suspend fun getRecentCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecent(station: RecentStationEntity)

    @Query("DELETE FROM recent_stations WHERE id = :stationId")
    suspend fun deleteRecentById(stationId: String)

    @Query("DELETE FROM recent_stations")
    suspend fun clearAllRecents()
}
