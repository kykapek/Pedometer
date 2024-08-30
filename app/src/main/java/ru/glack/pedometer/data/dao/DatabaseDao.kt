package ru.glack.pedometer.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ru.glack.pedometer.data.CoordinatesEntity
import ru.glack.pedometer.data.StepEntity

@Dao
interface DatabaseDao {

    @Insert
    suspend fun insertCoordinates(coordinate: CoordinatesEntity)

    @Insert
    suspend fun insertStep(step: StepEntity)

    @Query("SELECT * FROM coordinates")
    suspend fun getAllCoordinates(): List<CoordinatesEntity>

    @Query("SELECT * FROM steps")
    suspend fun getAllSteps(): List<StepEntity>

    // Новый метод для получения первого шага за текущий день
    @Query("SELECT * FROM steps WHERE date(timestamp) = date('now') ORDER BY timestamp ASC LIMIT 1")
    suspend fun getFirstStepOfToday(): StepEntity?

    // Новый метод для получения последнего шага за текущий день
    @Query("SELECT * FROM steps WHERE date(timestamp) = date('now') ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastStepOfToday(): StepEntity?
}