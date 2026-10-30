package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CheckInEntity::class,
        DailyTaskEntity::class,
        WeeklyPlanEntity::class,
        WeeklyTaskEntity::class,
        WeeklyReviewEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class CompassDatabase : RoomDatabase() {

    abstract fun compassDao(): CompassDao

    companion object {
        @Volatile
        private var INSTANCE: CompassDatabase? = null

        fun getInstance(context: Context): CompassDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CompassDatabase::class.java,
                    "sosyal_pusula_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
