package com.arar.muslim.presentation.quran

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import com.arar.muslim.domain.model.Ayah
import com.arar.muslim.domain.model.Surah
import com.arar.muslim.presentation.common.AyahEndBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AyahViewerScreen(
    surahId: Int,
    viewModel: QuranViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(surahId) {
        viewModel.selectSurah(surahId)
    }

    val selectedSurah by viewModel.selectedSurah.collectAsState()
    val ayahs by viewModel.selectedSurahAyahs.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = selectedSurah?.nameLatin ?: "Al-Qur'an",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        selectedSurah?.let {
                            Text(
                                text = "${it.nameTranslation} • ${it.totalAyah} Ayat",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = IslamicGreenPrimary
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Surah Header Card
            selectedSurah?.let { surah ->
                item {
                    SurahHeaderCard(surah = surah)
                }

                // Basmalah Banner (except Surah 9 At-Tawbah and Surah 1 Al-Fatihah where it's ayah 1)
                if (surah.number != 1 && surah.number != 9) {
                    item {
                        BasmalahCard()
                    }
                }
            }

            // List of Ayahs
            items(ayahs, key = { it.id }) { ayah ->
                AyahCard(
                    ayah = ayah,
                    surahName = selectedSurah?.nameLatin ?: "",
                    onBookmarkToggle = {
                        viewModel.toggleBookmark(ayah, selectedSurah?.nameLatin ?: "")
                        val msg = if (ayah.isBookmarked) "Bookmark dihapus" else "Bookmark disimpan"
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    },
                    onSaveLastRead = {
                        viewModel.saveLastRead(ayah, selectedSurah?.nameLatin ?: "")
                        Toast.makeText(context, "Ditandai sebagai bacaan terakhir", Toast.LENGTH_SHORT).show()
                    },
                    onCopyAyah = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText(
                            "Ayat Al-Qur'an",
                            "${ayah.textUthmani}\n\nArtinya:\n${ayah.translationId}\n(QS. ${selectedSurah?.nameLatin}: ${ayah.ayahNumber})"
                        )
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Ayat disalin ke clipboard", Toast.LENGTH_SHORT).show()
                    },
                    onShareAyah = {
                        val shareText = "${ayah.textUthmani}\n\nArtinya:\n${ayah.translationId}\n\n(QS. ${selectedSurah?.nameLatin} [${selectedSurah?.number}]: ${ayah.ayahNumber})\nDibagikan dari ArAr Muslim"
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(intent, "Bagikan Ayat"))
                    }
                )
            }
        }
    }
}

@Composable
fun SurahHeaderCard(surah: Surah) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = IslamicGreenPrimary
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = surah.nameArabic,
                fontFamily = QuranFontFamily,
                fontSize = 32.sp,
                color = IslamicGold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = surah.nameLatin,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = surah.nameTranslation,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
            Divider(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(180.dp),
                color = IslamicGold.copy(alpha = 0.5f),
                thickness = 1.dp
            )
            Text(
                text = "${surah.revelationType.uppercase()} • ${surah.totalAyah} AYAT • HALAMAN ${surah.pageNumber}",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
fun BasmalahCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Text(
                text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ",
                fontFamily = QuranFontFamily,
                fontSize = 24.sp,
                lineHeight = 48.sp,
                color = IslamicGreenPrimary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AyahCard(
    ayah: Ayah,
    surahName: String,
    onBookmarkToggle: () -> Unit,
    onSaveLastRead: () -> Unit,
    onCopyAyah: () -> Unit,
    onShareAyah: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Action Bar (Ayah Number, Bookmark, Copy, Share, Last Read)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AyahEndBadge(ayahNumber = ayah.ayahNumber)
                    if (ayah.sajdah) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = IslamicGold.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "Sajdah ۩",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = IslamicGreenPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row {
                    // Mark as Last Read
                    IconButton(onClick = onSaveLastRead, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = "Tandai Bacaan",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Copy
                    IconButton(onClick = onCopyAyah, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Salin",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Share
                    IconButton(onClick = onShareAyah, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Bagikan",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Bookmark
                    IconButton(onClick = onBookmarkToggle, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = if (ayah.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (ayah.isBookmarked) IslamicGold else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Teks Arab Mushaf Standar Kemenag RI (LPMQ Isep Misbah)
            // Layout RTL eksplisit dengan line-height minimal 2x font-size (26.sp & 52.sp)
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Text(
                    text = ayah.textUthmani,
                    fontFamily = QuranFontFamily,
                    fontSize = 26.sp,
                    lineHeight = 52.sp,
                    textAlign = TextAlign.Justify,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Terjemahan Resmi Kemenag RI
            Text(
                text = ayah.translationId,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
