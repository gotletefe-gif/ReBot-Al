package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteStationDao {
    @Query("SELECT * FROM favorite_stations ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteStationEntity>>

    @Query("SELECT id FROM favorite_stations")
    fun getAllFavoriteIds(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM favorite_stations")
    fun getFavoriteCount(): Flow<Int>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_stations WHERE id = :stationId)")
    fun isFavorite(stationId: String): Flow<Boolean>

    @Query("SELECT * FROM favorite_stations WHERE id = :stationId LIMIT 1")
    suspend fun getFavoriteById(stationId: String): FavoriteStationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(station: FavoriteStationEntity)

    @Query("DELETE FROM favorite_stations WHERE id = :stationId")
    suspend fun deleteFavoriteById(stationId: String)

    @Delete
    suspend fun deleteFavorite(station: FavoriteStationEntity)

    @Query("DELETE FROM favorite_stations")
    suspend fun clearAllFavorites()
}
