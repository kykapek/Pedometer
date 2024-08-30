package ru.glack.pedometer.ui.database

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.glack.pedometer.PedometerApp
import ru.glack.pedometer.core.DatabaseSender

class DatabaseViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = PedometerApp.getPreferences()

    private val _ipAddress = MutableStateFlow(prefs?.getString("ip_address", "") ?: "")
    val ipAddress: StateFlow<String> get() = _ipAddress

    private val _port = MutableStateFlow(prefs?.getString("port", "") ?: "")
    val port: StateFlow<String> get() = _port

    private val _username = MutableStateFlow(prefs?.getString("username", "") ?: "")
    val username: StateFlow<String> get() = _username

    private val _password = MutableStateFlow(prefs?.getString("password", "") ?: "")
    val password: StateFlow<String> get() = _password

    private val _sendDataState = MutableStateFlow<SendDataState>(SendDataState.None)
    val sendDataState: StateFlow<SendDataState> get() = _sendDataState

    fun savePreferences(ip: String, port: String, username: String, password: String) {
        Log.d("ghjkl", ip)
        prefs?.edit()?.apply {
            putString("ip_address", ip)
            putString("port", port)
            putString("username", username)
            putString("password", password)
            apply()
        }
        _ipAddress.value = ip
        _port.value = port
        _username.value = username
        _password.value = password
    }

    fun sendDataToServer(ip: String, port: String, user: String, password: String) {
        viewModelScope.launch {
            _sendDataState.value = SendDataState.Loading
            try {
                val coordinates =
                    PedometerApp.getInstance().getDatabase().databaseDao().getAllCoordinates()
                val steps = PedometerApp.getInstance().getDatabase().databaseDao().getAllSteps()
                val sender = DatabaseSender(ip, port, user, password)
                sender.sendCoordinates(coordinates)
                sender.sendSteps(steps)
                _sendDataState.value = SendDataState.Success
            } catch (e: Exception) {
                _sendDataState.value = SendDataState.Error(e.message.toString())
            }
        }
    }
}

sealed class SendDataState {
    object None : SendDataState()
    object Loading : SendDataState()
    object Success : SendDataState()
    data class Error(val errorMessage: String) : SendDataState()
}