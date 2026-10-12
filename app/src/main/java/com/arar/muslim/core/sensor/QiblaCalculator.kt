package com.arar.muslim.core.sensor

import kotlin.math.*

object QiblaCalculator {
    const val KAABA_LATITUDE = 21.422487
    const val KAABA_LONGITUDE = 39.826206

    /**
     * Menghitung azimut kiblat terhadap True North menggunakan Geometri Bola (Great-Circle Bearing).
     */
    fun calculateQiblaBearing(userLat: Double, userLng: Double): Double {
        val phi1 = Math.toRadians(userLat)
        val phi2 = Math.toRadians(KAABA_LATITUDE)
        val deltaLambda = Math.toRadians(KAABA_LONGITUDE - userLng)

        val y = sin(deltaLambda)
        val x = cos(phi1) * tan(phi2) - sin(phi1) * cos(deltaLambda)

        val bearingRad = atan2(y, x)
        return (Math.toDegrees(bearingRad) + 360.0) % 360.0
    }

    /**
     * Menghitung deviasi sudut putar jarum indikator.
     * targetAngle: sudut kiblat terhadap true north.
     * currentHeading: arah hadap ponsel terhadap true north.
     */
    fun calculateNeedleRotation(targetAngle: Double, currentHeading: Float): Float {
        var diff = (targetAngle - currentHeading).toFloat()
        while (diff > 180f) diff -= 360f
        while (diff < -180f) diff += 360f
        return diff
    }

    /**
     * Menghitung jarak perkiraan ke Ka'bah dalam kilometer menggunakan rumus Haversine.
     */
    fun calculateDistanceToKaaba(userLat: Double, userLng: Double): Double {
        val r = 6371.0 // Radius bumi dalam km
        val dLat = Math.toRadians(KAABA_LATITUDE - userLat)
        val dLon = Math.toRadians(KAABA_LONGITUDE - userLng)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(userLat)) * cos(Math.toRadians(KAABA_LATITUDE)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
