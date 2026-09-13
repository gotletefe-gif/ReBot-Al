package com.example.data.local

import com.example.model.RadioStation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoriteStationsRepository(private val favoriteStationDao: FavoriteStationDao) {

    val allFavorites: Flow<List<RadioStation>> = favoriteStationDao.getAllFavorites().map { entities ->
        entities.map { it.toRadioStation() }
    }

    val favoriteIds: Flow<Set<String>> = favoriteStationDao.getAllFavoriteIds().map { it.toSet() }

    val favoriteCount: Flow<Int> = favoriteStationDao.getFavoriteCount()

    fun isFavorite(stationId: String): Flow<Boolean> = favoriteStationDao.isFavorite(stationId)

    suspend fun addFavorite(station: RadioStation) {
        favoriteStationDao.insertFavorite(FavoriteStationEntity.fromRadioStation(station))
    }

    suspend fun removeFavorite(stationId: String) {
        favoriteStationDao.deleteFavoriteById(stationId)
    }

    suspend fun toggleFavorite(station: RadioStation, currentlyFavorite: Boolean): Boolean {
        return if (currentlyFavorite) {
            favoriteStationDao.deleteFavoriteById(station.id)
            false
        } else {
            favoriteStationDao.insertFavorite(FavoriteStationEntity.fromRadioStation(station))
            true
        }
    }

    suspend fun clearAllFavorites() {
        favoriteStationDao.clearAllFavorites()
    }
}
