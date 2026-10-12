package com.arar.muslim.presentation.quran

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.arar.muslim.R
import com.arar.muslim.core.theme.IslamicGold
import com.arar.muslim.core.theme.IslamicGreenPrimary
import com.arar.muslim.core.theme.QuranFontFamily
import com.arar.muslim.domain.model.Bookmark
import com.arar.muslim.domain.model.LastRead
import com.arar.muslim.domain.model.Surah
import com.arar.muslim.presentation.common.NumberBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranScreen(
    viewModel: QuranViewModel,
    onSurahClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Surah", "Juz", "Bookmark")

    val surahs by viewModel.surahsList.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val lastRead by viewModel.lastRead.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Top Bar / Header
        Surface(
            color = IslamicGreenPrimary,
            contentColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_arar_emblem),
                            contentDescription = "ArAr Muslim Logo",
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "ArAr Muslim",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = IslamicGold
                            )
                            Text(
                                text = "Al-Qur'anul Karim (100% Offline)",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = {
                        Text(
                            text = "Cari surah (e.g. Al-Kahf, Yasin, 18)...",
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
                        if (searchQuery.isNotEmpty()) {
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

        // Tab Row
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = IslamicGreenPrimary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                )
            }
        }

        // Tab Content
        when (selectedTabIndex) {
            0 -> SurahTabContent(
                surahs = surahs,
                lastRead = lastRead,
                onSurahClick = onSurahClick
            )
            1 -> JuzTabContent(
                onJuzClick = { juzNumber ->
                    // Open surah that starts this juz
                    val startSurah = getStartSurahForJuz(juzNumber)
                    onSurahClick(startSurah)
                }
            )
            2 -> BookmarkTabContent(
                bookmarks = bookmarks,
                onBookmarkClick = { onSurahClick(it.surahId) }
            )
        }
    }
}

@Composable
fun SurahTabContent(
    surahs: List<Surah>,
    lastRead: LastRead?,
    onSurahClick: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Last Read Banner
        lastRead?.let { lr ->
            item {
                LastReadCard(
                    lastRead = lr,
                    onClick = { onSurahClick(lr.surahId) }
                )
            }
        }

        items(surahs, key = { it.id }) { surah ->
            SurahListItem(
                surah = surah,
                onClick = { onSurahClick(surah.id) }
            )
        }
    }
}

@Composable
fun LastReadCard(
    lastRead: LastRead,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = IslamicGreenPrimary
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = "Last Read",
                        tint = IslamicGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Terakhir Dibaca",
                        fontSize = 12.sp,
                        color = IslamicGold,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${lastRead.surahName} : Ayat ${lastRead.ayahNumber}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Juz ${lastRead.juz}",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = IslamicGold,
                    contentColor = IslamicGreenPrimary
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Lanjut",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun SurahListItem(
    surah: Surah,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NumberBadge(number = surah.number)

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = surah.nameLatin,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${surah.nameTranslation} • ${surah.revelationType} • ${surah.totalAyah} Ayat",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = surah.nameArabic,
                fontFamily = QuranFontFamily,
                fontSize = 22.sp,
                color = IslamicGreenPrimary,
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
fun JuzTabContent(
    onJuzClick: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items((1..30).toList()) { juzNumber ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onJuzClick(juzNumber) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        NumberBadge(number = juzNumber)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Juz $juzNumber",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = getJuzDescription(juzNumber),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = "جزء $juzNumber",
                        fontFamily = QuranFontFamily,
                        fontSize = 18.sp,
                        color = IslamicGreenPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun BookmarkTabContent(
    bookmarks: List<Bookmark>,
    onBookmarkClick: (Bookmark) -> Unit
) {
    if (bookmarks.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Belum Ada Bookmark",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Tandai ayat favorit Anda saat membaca Al-Qur'an",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(bookmarks, key = { it.id }) { b ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onBookmarkClick(b) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${b.surahName} : Ayat ${b.ayahNumber}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = IslamicGreenPrimary
                            )
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = null,
                                tint = IslamicGold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = b.textPreview,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}

fun getStartSurahForJuz(juz: Int): Int {
    return when (juz) {
        1 -> 1; 2 -> 2; 3 -> 2; 4 -> 3; 5 -> 4; 6 -> 4; 7 -> 5; 8 -> 6; 9 -> 7; 10 -> 8
        11 -> 9; 12 -> 11; 13 -> 12; 14 -> 15; 15 -> 17; 16 -> 18; 17 -> 21; 18 -> 23; 19 -> 25; 20 -> 27
        21 -> 29; 22 -> 33; 23 -> 36; 24 -> 39; 25 -> 41; 26 -> 46; 27 -> 51; 28 -> 58; 29 -> 67; 30 -> 78
        else -> 1
    }
}

fun getJuzDescription(juz: Int): String {
    return when (juz) {
        1 -> "Al-Fatihah 1 - Al-Baqarah 141"
        2 -> "Al-Baqarah 142 - Al-Baqarah 252"
        3 -> "Al-Baqarah 253 - Ali 'Imran 92"
        4 -> "Ali 'Imran 93 - An-Nisa' 23"
        5 -> "An-Nisa' 24 - An-Nisa' 147"
        6 -> "An-Nisa' 148 - Al-Ma'idah 81"
        7 -> "Al-Ma'idah 82 - Al-An'am 110"
        8 -> "Al-An'am 111 - Al-A'raf 87"
        9 -> "Al-A'raf 88 - Al-Anfal 40"
        10 -> "Al-Anfal 41 - At-Taubah 92"
        11 -> "At-Taubah 93 - Hud 5"
        12 -> "Hud 6 - Yusuf 52"
        13 -> "Yusuf 53 - Ibrahim 52"
        14 -> "Al-Hijr 1 - An-Nahl 128"
        15 -> "Al-Isra' 1 - Al-Kahf 74"
        16 -> "Al-Kahf 75 - Ta Ha 135"
        17 -> "Al-Anbiya' 1 - Al-Hajj 78"
        18 -> "Al-Mu'minun 1 - Al-Furqan 20"
        19 -> "Al-Furqan 21 - An-Naml 55"
        20 -> "An-Naml 56 - Al-'Ankabut 45"
        21 -> "Al-'Ankabut 46 - Al-Ahzab 30"
        22 -> "Al-Ahzab 31 - Ya Sin 27"
        23 -> "Ya Sin 28 - Az-Zumar 31"
        24 -> "Az-Zumar 32 - Fussilat 46"
        25 -> "Fussilat 47 - Al-Jasiyah 37"
        26 -> "Al-Ahqaf 1 - Az-Zariyat 30"
        27 -> "Az-Zariyat 31 - Al-Hadid 29"
        28 -> "Al-Mujadilah 1 - At-Tahrim 12"
        29 -> "Al-Mulk 1 - Al-Mursalat 50"
        30 -> "An-Naba' 1 - An-Nas 6 (Juz 'Amma)"
        else -> ""
    }
}
