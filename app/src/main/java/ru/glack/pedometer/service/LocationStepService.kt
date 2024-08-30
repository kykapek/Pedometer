package ru.glack.pedometer.service


import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.glack.pedometer.PedometerApp
import ru.glack.pedometer.R
import ru.glack.pedometer.core.CHILD_LOCATION
import ru.glack.pedometer.core.CHILD_STEPS
import ru.glack.pedometer.core.convertMillisToDate
import ru.glack.pedometer.core.convertTimestamp
import ru.glack.pedometer.data.database.AppDatabase
import ru.glack.pedometer.data.CoordinatesEntity
import ru.glack.pedometer.data.EnumStepState
import ru.glack.pedometer.data.StepEntity
import ru.glack.pedometer.data.model.CoordinatesModel
import ru.glack.pedometer.data.model.StepModel
import ru.glack.pedometer.data.model.StepsModel
import ru.glack.pedometer.ui.MainActivity

class LocationStepService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var stepSensorManager: SensorManager
    private lateinit var database: AppDatabase
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private val prefs = PedometerApp.getPreferences()

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "location_step_service_channel"
        private const val NOTIFICATION_ID = 1

        private const val INTERVAL_MILLIS = 20 * 60 * 1000L // 20 минут
        //private const val INTERVAL_MILLIS = 30 * 1000L // 30 sec
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForegroundService()
        initializeServices()
        setupStepListener()
        setupPeriodicLocationUpdate()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Location and Step Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun startForegroundService() {
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Location and Step Service")
            .setContentText("Tracking location and steps.")
            .setSmallIcon(R.drawable.ic_service_screen)
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun initializeServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        stepSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        database = AppDatabase.getDatabase(applicationContext)
    }

    private fun setupStepListener() {
        val stepSensor = stepSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        val stepListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    CoroutineScope(Dispatchers.IO).launch {
                        val stepEntity =
                            StepEntity(timestamp = System.currentTimeMillis().toString())
                        database.databaseDao().insertStep(stepEntity)
                        sendStepsToServer()
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // Do nothing
            }
        }
        stepSensorManager.registerListener(
            stepListener,
            stepSensor,
            SensorManager.SENSOR_DELAY_FASTEST
        )
    }

    private fun setupPeriodicLocationUpdate() {
        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                fetchLocationAndUpdateDatabase()
                handler.postDelayed(this, INTERVAL_MILLIS)
            }
        }
        handler.post(runnable)
    }

    private fun fetchLocationAndUpdateDatabase() {
        if (ActivityCompat.checkSelfPermission(
                this,
                ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Handle the case where the permission is not granted
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                CoroutineScope(Dispatchers.IO).launch {
                    val coordinates = CoordinatesEntity(
                        longitude = it.longitude.toString(),
                        latitude = it.latitude.toString()
                    )
                    database.databaseDao().insertCoordinates(coordinates)
                    sendLocationDataToServer(
                        CoordinatesModel(
                            longitude = it.longitude.toString(),
                            latitude = it.latitude.toString(),
                            timestamp = convertTimestamp(System.currentTimeMillis())
                        )
                    )
                }
            }
        }
    }

    private fun sendLocationDataToServer(coordinates: CoordinatesModel) {
        PedometerApp.getInstance().getFirebaseDatabase()
            .child(PedometerApp.getInstance().getUserId() ?: "123")
            .child(CHILD_LOCATION)
            .setValue(coordinates)
    }

    private suspend fun sendStepsToServer() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = withContext(Dispatchers.IO) {
                getTodayStepsModel()
            }
            PedometerApp.getInstance().getFirebaseDatabase()
                .child(PedometerApp.getInstance().getUserId() ?: "123")
                .child(CHILD_STEPS)
                .setValue(result)
        }
    }

    suspend fun getTodayStepsModel(): StepsModel? {
        val firstStepEntity = database.databaseDao().getFirstStepOfToday()
        val lastStepEntity = database.databaseDao().getLastStepOfToday()
        if (firstStepEntity == null || lastStepEntity == null) {
            return null
        }
        val firstStepModel = StepModel(
            stepState = EnumStepState.FIRST_STEP,
            timestamp = firstStepEntity.timestamp
        )
        val lastStepModel = StepModel(
            stepState = EnumStepState.LAST_STEP,
            timestamp = lastStepEntity.timestamp
        )
        return StepsModel(
            date = convertMillisToDate(firstStepEntity.timestamp.toLong()),
            firstStep = firstStepModel,
            lastStep = lastStepModel
        )
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
        //stepSensorManager.unregisterListener(null)
    }
}