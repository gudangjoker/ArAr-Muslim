package com.arar.muslim.domain.model

data class Surah(
    val id: Int,
    val number: Int,
    val nameArabic: String,
    val nameLatin: String,
    val nameTranslation: String,
    val revelationType: String,
    val totalAyah: Int,
    val pageNumber: Int
)

data class Ayah(
    val id: Int,
    val surahId: Int,
    val ayahNumber: Int,
    val textUthmani: String,
    val translationId: String,
    val juz: Int,
    val page: Int,
    val manzil: Int,
    val sajdah: Boolean,
    val isBookmarked: Boolean = false
)

data class AsmaulHusna(
    val number: Int,
    val arabic: String,
    val latin: String,
    val translationId: String,
    val meaningDetail: String
)

data class Doa(
    val id: Int,
    val category: String,
    val title: String,
    val arabic: String,
    val transliteration: String,
    val translationId: String,
    val reference: String
)

data class Bookmark(
    val id: Int,
    val surahId: Int,
    val ayahNumber: Int,
    val surahName: String,
    val textPreview: String,
    val timestamp: Long
)

data class LastRead(
    val surahId: Int,
    val ayahNumber: Int,
    val surahName: String,
    val juz: Int,
    val timestamp: Long
)

data class QiblaData(
    val qiblaBearing: Double = 0.0,
    val currentHeading: Float = 0f,
    val needleRotation: Float = 0f,
    val distanceKm: Double = 0.0,
    val accuracyStatus: Int = 0,
    val needsCalibration: Boolean = false,
    val isPointingToKaaba: Boolean = false
)
