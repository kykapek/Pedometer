package ru.glack.pedometer.core

import ru.glack.pedometer.data.CoordinatesEntity
import ru.glack.pedometer.data.StepEntity
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.SQLException

class DatabaseSender(
    private val ip: String,
    private val port: String,
    private val user: String,
    private val password: String
) {

    private fun getConnection(): Connection {
        val url = "jdbc:jtds:sqlserver://$ip:$port/your_database"
        return DriverManager.getConnection(url, user, password)
    }

    fun sendCoordinates(coordinates: List<CoordinatesEntity>) {
        val connection = getConnection()
        val query = "INSERT INTO Coordinates (longitude, latitude) VALUES (?, ?)"
        try {
            connection.autoCommit = false
            val preparedStatement: PreparedStatement = connection.prepareStatement(query)
            for (coordinate in coordinates) {
                preparedStatement.setString(1, coordinate.longitude)
                preparedStatement.setString(2, coordinate.latitude)
                preparedStatement.addBatch()
            }
            preparedStatement.executeBatch()
            connection.commit()
        } catch (e: SQLException) {
            e.printStackTrace()
            connection.rollback()
        } finally {
            connection.close()
        }
    }

    fun sendSteps(steps: List<StepEntity>) {
        val connection = getConnection()
        val query = "INSERT INTO Steps (timestamp) VALUES (?)"
        try {
            connection.autoCommit = false
            val preparedStatement: PreparedStatement = connection.prepareStatement(query)
            for (step in steps) {
                preparedStatement.setString(1, step.timestamp)
                preparedStatement.addBatch()
            }
            preparedStatement.executeBatch()
            connection.commit()
        } catch (e: SQLException) {
            e.printStackTrace()
            connection.rollback()
        } finally {
            connection.close()
        }
    }
}