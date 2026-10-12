package com.arar.muslim.domain.repository

import com.arar.muslim.domain.model.AsmaulHusna
import com.arar.muslim.domain.model.Ayah
import com.arar.muslim.domain.model.Bookmark
import com.arar.muslim.domain.model.Doa
import com.arar.muslim.domain.model.LastRead
import com.arar.muslim.domain.model.QiblaData
import com.arar.muslim.domain.model.Surah
import kotlinx.coroutines.flow.Flow

interface QuranRepository {
    fun getAllSurahs(): Flow<List<Surah>>
    suspend fun getSurahById(id: Int): Surah?
    fun searchSurahs(query: String): Flow<List<Surah>>
    fun getAyahsBySurah(surahId: Int): Flow<List<Ayah>>
    fun getAyahsByJuz(juz: Int): Flow<List<Ayah>>
    fun searchAyahs(query: String): Flow<List<Ayah>>
}

interface AsmaulHusnaRepository {
    fun getAllAsmaulHusna(): Flow<List<AsmaulHusna>>
    fun searchAsmaulHusna(query: String): Flow<List<AsmaulHusna>>
}

interface DoaRepository {
    fun getAllDoa(): Flow<List<Doa>>
    fun getDoaByCategory(category: String): Flow<List<Doa>>
    fun getCategories(): Flow<List<String>>
}

interface UserDataRepository {
    fun getAllBookmarks(): Flow<List<Bookmark>>
    fun isBookmarked(surahId: Int, ayahNumber: Int): Flow<Boolean>
    suspend fun toggleBookmark(surahId: Int, ayahNumber: Int, surahName: String, textPreview: String)
    fun getLastRead(): Flow<LastRead?>
    suspend fun saveLastRead(surahId: Int, ayahNumber: Int, surahName: String, juz: Int)
}

interface QiblaRepository {
    val qiblaState: Flow<QiblaData>
    fun startCompass()
    fun stopCompass()
    fun updateLocation(lat: Double, lng: Double)
}
