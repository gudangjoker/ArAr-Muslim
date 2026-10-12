package com.arar.muslim

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.arar.muslim.core.theme.ArArMuslimTheme
import com.arar.muslim.presentation.asmaulhusna.AsmaulHusnaViewModel
import com.arar.muslim.presentation.doa.DzikirViewModel
import com.arar.muslim.presentation.navigation.AppNavigation
import com.arar.muslim.presentation.qibla.QiblaViewModel
import com.arar.muslim.presentation.quran.QuranViewModel

class MainActivity : ComponentActivity() {

    private lateinit var qiblaViewModel: QiblaViewModel
    private lateinit var quranViewModel: QuranViewModel
    private lateinit var asmaulHusnaViewModel: AsmaulHusnaViewModel
    private lateinit var dzikirViewModel: DzikirViewModel

    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            updateUserLocation()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as ArArMuslimApp
        quranViewModel = QuranViewModel(app.quranRepository, app.userDataRepository)
        qiblaViewModel = QiblaViewModel(app.qiblaRepository)
        asmaulHusnaViewModel = AsmaulHusnaViewModel(app.asmaulHusnaRepository)
        dzikirViewModel = DzikirViewModel(app.doaRepository)

        checkAndRequestLocationPermission()

        setContent {
            ArArMuslimTheme {
                val navController = rememberNavController()
                AppNavigation(
                    navController = navController,
                    quranViewModel = quranViewModel,
                    qiblaViewModel = qiblaViewModel,
                    asmaulHusnaViewModel = asmaulHusnaViewModel,
                    dzikirViewModel = dzikirViewModel
                )
            }
        }
    }

    private fun checkAndRequestLocationPermission() {
        val fineLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val coarseLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (fineLocationPermission == PackageManager.PERMISSION_GRANTED ||
            coarseLocationPermission == PackageManager.PERMISSION_GRANTED
        ) {
            updateUserLocation()
        } else {
            requestLocationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun updateUserLocation() {
        try {
            val locationManager = getSystemService(LOCATION_SERVICE) as? LocationManager ?: return
            val lastGps: Location? = try {
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            } catch (_: SecurityException) { null }

            val lastNetwork: Location? = try {
                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            } catch (_: SecurityException) { null }

            val bestLocation = lastGps ?: lastNetwork
            bestLocation?.let {
                qiblaViewModel.updateCoordinates(it.latitude, it.longitude)
            }
        } catch (_: Exception) {}
    }
}
