package com.arar.muslim.presentation.doa

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arar.muslim.core.theme.IslamicGold
import com.arar.muslim.core.theme.IslamicGreenPrimary
import com.arar.muslim.core.theme.QuranFontFamily
import com.arar.muslim.domain.model.Doa

@Composable
fun DzikirScreen(
    viewModel: DzikirViewModel,
    modifier: Modifier = Modifier
) {
    val doaList by viewModel.doaList.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val filterKeyword by viewModel.filterKeyword.collectAsState()
    val counters by viewModel.counters.collectAsState()
    val context = LocalContext.current

    val categories = listOf("Pagi-Petang", "Harian", "Sholat", "Semua")

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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Doa & Dzikir",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = IslamicGold
                )
                Text(
                    text = "Hisnul Muslim, Dzikir Pagi-Petang & Doa Harian",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                OutlinedTextField(
                    value = filterKeyword,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = {
                        Text(
                            text = "Cari doa atau dzikir...",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.White
                        )
                    },
                    trailingIcon = {
                        if (filterKeyword.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = Color.White
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.White.copy(alpha = 0.15f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                        focusedBorderColor = IslamicGold,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        cursorColor = IslamicGold
                    ),
                    singleLine = true
                )
            }
        }

        // Category Filter Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                FilterChip(
                    selected = (selectedCategory == category),
                    onClick = { viewModel.selectCategory(category) },
                    label = { Text(category, fontSize = 13.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IslamicGreenPrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // List of Doa Cards
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(doaList, key = { it.id }) { item ->
                val currentCount = counters[item.id] ?: 0
                DzikirCard(
                    doa = item,
                    count = currentCount,
                    onCountClick = {
                        viewModel.incrementCounter(item.id, targetFromTitle(item.title))
                        triggerTick(context)
                    },
                    onResetClick = {
                        viewModel.resetCounter(item.id)
                    }
                )
            }
        }
    }
}

@Composable
fun DzikirCard(
    doa: Doa,
    count: Int,
    onCountClick: () -> Unit,
    onResetClick: () -> Unit
) {
    val target = targetFromTitle(doa.title)
    val isCompleted = count >= target

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Category & Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = IslamicGreenPrimary.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = doa.category,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = IslamicGreenPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                if (target > 1) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isCompleted) IslamicGreenPrimary else IslamicGold.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = if (isCompleted) "Selesai ($count/$target)" else "Target: $target kali",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCompleted) Color.White else Color(0xFF856404),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = doa.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Teks Arab
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Text(
                    text = doa.arabic,
                    fontFamily = QuranFontFamily,
                    fontSize = 24.sp,
                    lineHeight = 48.sp,
                    textAlign = TextAlign.Justify,
                    color = IslamicGreenPrimary,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Transliterasi (jika ada)
            if (doa.transliteration.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = doa.transliteration,
                    fontSize = 13.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Arti
            Text(
                text = doa.translationId,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Dalil / Referensi
            if (doa.reference.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Dalil: ${doa.reference}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = IslamicGold.copy(alpha = 0.9f)
                )
            }

            // Interactive Tasbih Counter Button
            if (target > 1) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onCountClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCompleted) Color(0xFF2E8B57) else IslamicGreenPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (isCompleted) "Alhamdulillah ($count/$target)" else "Tekan Tasbih ($count / $target)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    if (count > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = onResetClick) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

fun targetFromTitle(title: String): Int {
    return when {
        title.contains("100x") || title.contains("100") -> 100
        title.contains("33x") || title.contains("33") -> 33
        title.contains("10x") || title.contains("10") -> 10
        title.contains("7x") || title.contains("7") -> 7
        title.contains("3x") || title.contains("3") -> 3
        else -> 1
    }
}

private fun triggerTick(context: Context) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator?.vibrate(
                VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
            )
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            vibrator?.vibrate(20)
        }
    } catch (_: Exception) {}
}
