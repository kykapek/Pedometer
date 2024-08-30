package ru.glack.pedometer.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ru.glack.pedometer.data.CoordinatesEntity
import ru.glack.pedometer.data.StepEntity
import ru.glack.pedometer.data.dao.DatabaseDao

@Database(entities = [CoordinatesEntity::class, StepEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun databaseDao(): DatabaseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pedometer_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}