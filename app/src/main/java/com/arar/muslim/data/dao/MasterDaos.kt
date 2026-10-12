package com.arar.muslim.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.arar.muslim.data.entity.AsmaulHusnaEntity
import com.arar.muslim.data.entity.AyahEntity
import com.arar.muslim.data.entity.DoaEntity
import com.arar.muslim.data.entity.SurahEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SurahDao {
    @Query("SELECT * FROM surah ORDER BY number ASC")
    fun getAllSurahs(): Flow<List<SurahEntity>>

    @Query("SELECT * FROM surah WHERE id = :id LIMIT 1")
    suspend fun getSurahById(id: Int): SurahEntity?

    @Query("""
        SELECT * FROM surah 
        WHERE name_latin LIKE '%' || :query || '%' 
           OR name_translation LIKE '%' || :query || '%'
        ORDER BY number ASC
    """)
    fun searchSurahs(query: String): Flow<List<SurahEntity>>
}

@Dao
interface AyahDao {
    @Query("SELECT * FROM ayah WHERE surah_id = :surahId ORDER BY ayah_number ASC")
    fun getAyahsBySurah(surahId: Int): Flow<List<AyahEntity>>

    @Query("SELECT * FROM ayah WHERE juz = :juz ORDER BY surah_id ASC, ayah_number ASC")
    fun getAyahsByJuz(juz: Int): Flow<List<AyahEntity>>

    @Query("SELECT * FROM ayah WHERE surah_id = :surahId AND ayah_number = :ayahNumber LIMIT 1")
    suspend fun getAyah(surahId: Int, ayahNumber: Int): AyahEntity?

    @Query("""
        SELECT * FROM ayah 
        WHERE translation_id LIKE '%' || :query || '%' 
        LIMIT 50
    """)
    fun searchAyahs(query: String): Flow<List<AyahEntity>>
}

@Dao
interface AsmaulHusnaDao {
    @Query("SELECT * FROM asmaul_husna ORDER BY number ASC")
    fun getAllAsmaulHusna(): Flow<List<AsmaulHusnaEntity>>

    @Query("""
        SELECT * FROM asmaul_husna 
        WHERE latin LIKE '%' || :query || '%' 
           OR translation_id LIKE '%' || :query || '%'
        ORDER BY number ASC
    """)
    fun searchAsmaulHusna(query: String): Flow<List<AsmaulHusnaEntity>>
}

@Dao
interface DoaDao {
    @Query("SELECT * FROM doa ORDER BY id ASC")
    fun getAllDoa(): Flow<List<DoaEntity>>

    @Query("SELECT * FROM doa WHERE category = :category ORDER BY id ASC")
    fun getDoaByCategory(category: String): Flow<List<DoaEntity>>

    @Query("SELECT DISTINCT category FROM doa")
    fun getCategories(): Flow<List<String>>
}
