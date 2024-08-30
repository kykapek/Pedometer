package ru.glack.pedometer

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import ru.glack.pedometer.core.USER_UUID
import ru.glack.pedometer.data.database.AppDatabase
import java.util.UUID

class PedometerApp : Application() {

    private lateinit var appDatabase: AppDatabase
    private lateinit var firebaseDatabase: FirebaseDatabase
    private var prefs: SharedPreferences? = null


    override fun onCreate() {
        super.onCreate()
        instance = this
        firebaseDatabase = Firebase.database
        createPreferences()
        createUserId()
    }

    private fun createPreferences() {
        prefs = getSharedPreferences(
            resources.getString(R.string.app_name),
            Context.MODE_PRIVATE
        )
    }

    fun getDatabase(): AppDatabase {
        return AppDatabase.getDatabase(context = applicationContext)
    }

    fun getFirebaseDatabase(): DatabaseReference {
        return firebaseDatabase.reference
    }

    private fun createUserId() {
        if (getUserId().isNullOrEmpty()) {
            saveUserId()
        }
    }

    fun saveUserId() {
        val editor = prefs?.edit()
        editor?.putString(USER_UUID, UUID.randomUUID().toString())
        editor?.commit()
    }

    fun getUserId(): String? {
        return prefs?.getString(USER_UUID, "")
    }

    companion object {
        private const val PREFS_NAME = "PedometerPrefs"
        private var instance: PedometerApp? = null

        fun getInstance(): PedometerApp {
            return instance ?: throw IllegalStateException("Application not initialized")
        }

        fun getPreferences(): SharedPreferences? {
            return instance?.prefs
        }
    }
}