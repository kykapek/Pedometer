package ru.glack.pedometer.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "coordinates")
data class CoordinatesEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val longitude: String,
    val latitude: String
)