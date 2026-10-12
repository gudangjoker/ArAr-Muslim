package com.arar.muslim.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "surah",
    indices = [Index(value = ["number"], unique = true)]
)
data class SurahEntity(
    @PrimaryKey
    val id: Int,
    @ColumnInfo(name = "number")
    val number: Int,
    @ColumnInfo(name = "name_arabic")
    val nameArabic: String,
    @ColumnInfo(name = "name_latin")
    val nameLatin: String,
    @ColumnInfo(name = "name_translation")
    val nameTranslation: String,
    @ColumnInfo(name = "revelation_type")
    val revelationType: String,
    @ColumnInfo(name = "total_ayah")
    val totalAyah: Int,
    @ColumnInfo(name = "page_number")
    val pageNumber: Int
)

@Entity(
    tableName = "ayah",
    foreignKeys = [
        ForeignKey(
            entity = SurahEntity::class,
            parentColumns = ["id"],
            childColumns = ["surah_id"],
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index(value = ["surah_id"], name = "idx_ayah_surah"),
        Index(value = ["juz"], name = "idx_ayah_juz"),
        Index(value = ["page"], name = "idx_ayah_page")
    ]
)
data class AyahEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "surah_id")
    val surahId: Int,
    @ColumnInfo(name = "ayah_number")
    val ayahNumber: Int,
    @ColumnInfo(name = "text_uthmani")
    val textUthmani: String,
    @ColumnInfo(name = "translation_id")
    val translationId: String,
    @ColumnInfo(name = "juz")
    val juz: Int,
    @ColumnInfo(name = "page")
    val page: Int,
    @ColumnInfo(name = "manzil")
    val manzil: Int,
    @ColumnInfo(name = "sajdah")
    val sajdah: Int = 0
)

@Entity(
    tableName = "asmaul_husna",
    indices = [Index(value = ["number"], unique = true)]
)
data class AsmaulHusnaEntity(
    @PrimaryKey
    val id: Int,
    @ColumnInfo(name = "number")
    val number: Int,
    @ColumnInfo(name = "arabic")
    val arabic: String,
    @ColumnInfo(name = "latin")
    val latin: String,
    @ColumnInfo(name = "translation_id")
    val translationId: String,
    @ColumnInfo(name = "meaning_detail")
    val meaningDetail: String
)

@Entity(
    tableName = "doa",
    indices = [Index(value = ["category"], name = "idx_doa_category")]
)
data class DoaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "category")
    val category: String,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "arabic")
    val arabic: String,
    @ColumnInfo(name = "transliteration")
    val transliteration: String,
    @ColumnInfo(name = "translation_id")
    val translationId: String,
    @ColumnInfo(name = "reference")
    val reference: String
)

@Entity(tableName = "reciter")
data class ReciterEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    @ColumnInfo(name = "subfolder_path")
    val subfolderPath: String
)

@Entity(
    tableName = "audio_metadata",
    foreignKeys = [
        ForeignKey(
            entity = AyahEntity::class,
            parentColumns = ["id"],
            childColumns = ["ayah_id"]
        ),
        ForeignKey(
            entity = ReciterEntity::class,
            parentColumns = ["id"],
            childColumns = ["reciter_id"]
        )
    ],
    indices = [
        Index(value = ["ayah_id", "reciter_id"], unique = true, name = "idx_audio_ayah_reciter")
    ]
)
data class AudioMetadataEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "ayah_id")
    val ayahId: Int,
    @ColumnInfo(name = "reciter_id")
    val reciterId: Int,
    @ColumnInfo(name = "remote_url")
    val remoteUrl: String,
    @ColumnInfo(name = "local_path")
    val localPath: String? = null,
    @ColumnInfo(name = "duration_ms")
    val durationMs: Int = 0,
    @ColumnInfo(name = "file_size_bytes")
    val fileSizeBytes: Int = 0,
    @ColumnInfo(name = "download_status")
    val downloadStatus: Int = 0
)
