package com.arar.muslim.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmark")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val surahId: Int,
    val ayahNumber: Int,
    val surahName: String,
    val textPreview: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "last_read")
data class LastReadEntity(
    @PrimaryKey
    val id: Int = 1,
    val surahId: Int,
    val ayahNumber: Int,
    val surahName: String,
    val juz: Int,
    val timestamp: Long = System.currentTimeMillis()
)
