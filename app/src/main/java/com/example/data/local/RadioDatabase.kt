package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [FavoriteStationEntity::class, RecentStationEntity::class],
    version = 2,
    exportSchema = false
)
abstract class RadioDatabase : RoomDatabase() {
    abstract fun favoriteStationDao(): FavoriteStationDao
    abstract fun recentStationDao(): RecentStationDao

    companion object {
        @Volatile
        private var INSTANCE: RadioDatabase? = null

        fun getDatabase(context: Context): RadioDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RadioDatabase::class.java,
                    "radyo_turk_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
