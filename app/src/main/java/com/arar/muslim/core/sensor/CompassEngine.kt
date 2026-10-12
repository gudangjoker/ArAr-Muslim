package com.arar.muslim.core.sensor

import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CompassState(
    val trueHeading: Float = 0f,
    val accuracyStatus: Int = SensorManager.SENSOR_STATUS_UNRELIABLE,
    val needsCalibration: Boolean = false,
    val isSensorAvailable: Boolean = true
)

class CompassEngine(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private val _compassState = MutableStateFlow(
        CompassState(isSensorAvailable = (rotationVectorSensor != null))
    )
    val compassState = _compassState.asStateFlow()

    private var geomagneticField: GeomagneticField? = null
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    // Low-pass filter damping
    private var smoothedAzimuth = 0f
    private val alpha = 0.15f

    fun updateLocation(lat: Double, lng: Double, alt: Double = 0.0) {
        geomagneticField = GeomagneticField(
            lat.toFloat(),
            lng.toFloat(),
            alt.toFloat(),
            System.currentTimeMillis()
        )
    }

    fun startListening() {
        rotationVectorSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ROTATION_VECTOR) return

        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
        SensorManager.getOrientation(rotationMatrix, orientationAngles)

        // Azimuth dalam radian (-PI sampai PI) -> konversi ke derajat (0 sampai 360)
        val magneticAzimuth = (Math.toDegrees(orientationAngles[0].toDouble()).toFloat() + 360f) % 360f

        // Koreksi ke True North menggunakan deklinasi
        val declination = geomagneticField?.declination ?: 0f
        val rawTrueNorth = (magneticAzimuth + declination + 360f) % 360f

        // Low-pass filter untuk meredam jitter getaran tangan
        smoothedAzimuth = filterAzimuth(smoothedAzimuth, rawTrueNorth, alpha)

        val requiresCalibration = _compassState.value.accuracyStatus <= SensorManager.SENSOR_STATUS_ACCURACY_LOW

        _compassState.value = _compassState.value.copy(
            trueHeading = smoothedAzimuth,
            needsCalibration = requiresCalibration
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        _compassState.value = _compassState.value.copy(
            accuracyStatus = accuracy,
            needsCalibration = (accuracy <= SensorManager.SENSOR_STATUS_ACCURACY_LOW)
        )
    }

    private fun filterAzimuth(current: Float, target: Float, alpha: Float): Float {
        var delta = target - current
        if (delta > 180f) delta -= 360f
        else if (delta < -180f) delta += 360f
        return (current + alpha * delta + 360f) % 360f
    }
}
