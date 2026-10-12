package com.arar.muslim.presentation.qibla

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arar.muslim.R
import com.arar.muslim.core.theme.IslamicGold
import com.arar.muslim.core.theme.IslamicGreenPrimary
import kotlin.math.roundToInt

@Composable
fun QiblaScreen(
    viewModel: QiblaViewModel,
    modifier: Modifier = Modifier
) {
    val qiblaState by viewModel.qiblaState.collectAsState()
    val showCalibrationDialog by viewModel.showCalibrationDialog.collectAsState()
    val context = LocalContext.current

    DisposableEffect(Unit) {
        viewModel.onResume()
        onDispose {
            viewModel.onPause()
        }
    }

    // Trigger gentle vibration when pointer aligns with Kaaba
    LaunchedEffect(qiblaState.isPointingToKaaba) {
        if (qiblaState.isPointingToKaaba) {
            triggerVibration(context)
        }
    }

    // Smooth needle animation
    val animatedNeedleRotation by animateFloatAsState(
        targetValue = qiblaState.needleRotation,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "NeedleRotation"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Surface(
            color = IslamicGreenPrimary,
            contentColor = Color.White
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Arah Kiblat",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = IslamicGold
                    )
                    Text(
                        text = "Tilt-Compensated & True North",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                IconButton(onClick = { viewModel.setShowCalibrationDialog(true) }) {
                    Icon(
                        imageVector = Icons.Default.CompassCalibration,
                        contentDescription = "Kalibrasi",
                        tint = IslamicGold
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Alignment status banner
            AlignmentStatusBanner(
                isPointingToKaaba = qiblaState.isPointingToKaaba,
                needsCalibration = qiblaState.needsCalibration,
                onCalibrateClick = { viewModel.setShowCalibrationDialog(true) }
            )

            // Compass Dial Box
            Box(
                modifier = Modifier
                    .size(290.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background compass dial with cardinal points
                CompassDial(
                    heading = qiblaState.currentHeading,
                    targetBearing = qiblaState.qiblaBearing.toFloat(),
                    modifier = Modifier.fillMaxSize()
                )

                // Kaaba marker at target angle
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(animatedNeedleRotation),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_kaaba),
                        contentDescription = "Kaaba",
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .size(36.dp)
                            .padding(top = 10.dp)
                    )
                }

                // Rotating Needle Vector
                Icon(
                    painter = painterResource(id = R.drawable.ic_compass_needle),
                    contentDescription = "Compass Needle",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(170.dp)
                        .rotate(animatedNeedleRotation)
                )

                // Center Hub Badge
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            if (qiblaState.isPointingToKaaba) IslamicGreenPrimary else IslamicGold
                        )
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${qiblaState.currentHeading.roundToInt()}°",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Info Details Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoMetricCard(
                    title = "Sudut Kiblat",
                    value = "${qiblaState.qiblaBearing.roundToInt()}°",
                    subtitle = "dari True North",
                    modifier = Modifier.weight(1f)
                )

                InfoMetricCard(
                    title = "Jarak Ka'bah",
                    value = "${qiblaState.distanceKm.roundToInt()} km",
                    subtitle = "Mekkah Al-Mukarramah",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    // Calibration Dialog
    if (showCalibrationDialog) {
        CalibrationGuideDialog(
            onDismiss = { viewModel.setShowCalibrationDialog(false) }
        )
    }
}

@Composable
fun AlignmentStatusBanner(
    isPointingToKaaba: Boolean,
    needsCalibration: Boolean,
    onCalibrateClick: () -> Unit
) {
    if (isPointingToKaaba) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = IslamicGreenPrimary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = IslamicGold,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Tepat Menghadap Ka'bah!",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Arah ponsel Anda sudah selaras lurus dengan Kiblat.",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }
    } else if (needsCalibration) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFF856404),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Akurasi Sensor Rendah",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF856404)
                    )
                    Text(
                        text = "Gerakkan ponsel membentuk angka 8 untuk kalibrasi.",
                        fontSize = 11.sp,
                        color = Color(0xFF856404)
                    )
                }
                TextButton(onClick = onCalibrateClick) {
                    Text("Panduan", color = Color(0xFF856404), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    } else {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = IslamicGreenPrimary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Putar ponsel hingga jarum tepat mengarah ke Ka'bah.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CompassDial(
    heading: Float,
    targetBearing: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2 - 12.dp.toPx()

        // Outer Ring
        drawCircle(
            color = Color(0xFFE0E0E0),
            radius = radius,
            center = center,
            style = Stroke(width = 3.dp.toPx())
        )

        // Draw ticks every 30 degrees
        for (i in 0 until 360 step 30) {
            val angleRad = Math.toRadians((i - heading - 90).toDouble())
            val isCardinal = (i % 90 == 0)
            val tickLength = if (isCardinal) 14.dp.toPx() else 8.dp.toPx()

            val startX = center.x + (radius - tickLength) * kotlin.math.cos(angleRad).toFloat()
            val startY = center.y + (radius - tickLength) * kotlin.math.sin(angleRad).toFloat()
            val endX = center.x + radius * kotlin.math.cos(angleRad).toFloat()
            val endY = center.y + radius * kotlin.math.sin(angleRad).toFloat()

            drawLine(
                color = if (i == 0) Color(0xFFE53935) else Color(0xFF9E9E9E),
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = if (isCardinal) 3.dp.toPx() else 1.5.dp.toPx()
            )
        }
    }
}

@Composable
fun InfoMetricCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = IslamicGreenPrimary
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun CalibrationGuideDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CompassCalibration,
                    contentDescription = null,
                    tint = IslamicGreenPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Kalibrasi Kompas", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column {
                Text(
                    text = "Untuk akurasi maksimal penunjuk kiblat:",
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "1. Pegang ponsel dan gerakkan di udara membentuk angka delapan (∞) sebanyak 3-5 kali.\n\n" +
                           "2. Jauhkan dari medan magnetik seperti casing bermagnet, logam tebal, atau laptop.\n\n" +
                           "3. Pastikan fitur lokasi (GPS) aktif untuk kalkulasi True North otomatis.",
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = IslamicGreenPrimary)
            ) {
                Text("Mengerti")
            }
        }
    )
}

private fun triggerVibration(context: Context) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator?.vibrate(
                VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            vibrator?.vibrate(50)
        }
    } catch (_: Exception) {}
}
