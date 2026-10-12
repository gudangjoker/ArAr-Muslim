package com.arar.muslim.data.repository

import com.arar.muslim.core.sensor.CompassEngine
import com.arar.muslim.core.sensor.QiblaCalculator
import com.arar.muslim.data.dao.AsmaulHusnaDao
import com.arar.muslim.data.dao.AyahDao
import com.arar.muslim.data.dao.DoaDao
import com.arar.muslim.data.dao.SurahDao
import com.arar.muslim.data.dao.UserDataDao
import com.arar.muslim.data.entity.BookmarkEntity
import com.arar.muslim.data.entity.LastReadEntity
import com.arar.muslim.domain.model.AsmaulHusna
import com.arar.muslim.domain.model.Ayah
import com.arar.muslim.domain.model.Bookmark
import com.arar.muslim.domain.model.Doa
import com.arar.muslim.domain.model.LastRead
import com.arar.muslim.domain.model.QiblaData
import com.arar.muslim.domain.model.Surah
import com.arar.muslim.domain.repository.AsmaulHusnaRepository
import com.arar.muslim.domain.repository.DoaRepository
import com.arar.muslim.domain.repository.QiblaRepository
import com.arar.muslim.domain.repository.QuranRepository
import com.arar.muslim.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlin.math.abs

class QuranRepositoryImpl(
    private val surahDao: SurahDao,
    private val ayahDao: AyahDao,
    private val userDataDao: UserDataDao
) : QuranRepository {

    override fun getAllSurahs(): Flow<List<Surah>> {
        return surahDao.getAllSurahs().map { list ->
            list.map {
                Surah(
                    id = it.id,
                    number = it.number,
                    nameArabic = it.nameArabic,
                    nameLatin = it.nameLatin,
                    nameTranslation = it.nameTranslation,
                    revelationType = it.revelationType,
                    totalAyah = it.totalAyah,
                    pageNumber = it.pageNumber
                )
            }
        }
    }

    override suspend fun getSurahById(id: Int): Surah? {
        val entity = surahDao.getSurahById(id) ?: return null
        return Surah(
            id = entity.id,
            number = entity.number,
            nameArabic = entity.nameArabic,
            nameLatin = entity.nameLatin,
            nameTranslation = entity.nameTranslation,
            revelationType = entity.revelationType,
            totalAyah = entity.totalAyah,
            pageNumber = entity.pageNumber
        )
    }

    override fun searchSurahs(query: String): Flow<List<Surah>> {
        return surahDao.searchSurahs(query).map { list ->
            list.map {
                Surah(
                    id = it.id,
                    number = it.number,
                    nameArabic = it.nameArabic,
                    nameLatin = it.nameLatin,
                    nameTranslation = it.nameTranslation,
                    revelationType = it.revelationType,
                    totalAyah = it.totalAyah,
                    pageNumber = it.pageNumber
                )
            }
        }
    }

    override fun getAyahsBySurah(surahId: Int): Flow<List<Ayah>> {
        return combine(
            ayahDao.getAyahsBySurah(surahId),
            userDataDao.getAllBookmarks()
        ) { ayahs, bookmarks ->
            val bookmarkedSet = bookmarks.filter { it.surahId == surahId }.map { it.ayahNumber }.toSet()
            ayahs.map {
                Ayah(
                    id = it.id,
                    surahId = it.surahId,
                    ayahNumber = it.ayahNumber,
                    textUthmani = it.textUthmani,
                    translationId = it.translationId,
                    juz = it.juz,
                    page = it.page,
                    manzil = it.manzil,
                    sajdah = (it.sajdah == 1),
                    isBookmarked = bookmarkedSet.contains(it.ayahNumber)
                )
            }
        }
    }

    override fun getAyahsByJuz(juz: Int): Flow<List<Ayah>> {
        return combine(
            ayahDao.getAyahsByJuz(juz),
            userDataDao.getAllBookmarks()
        ) { ayahs, bookmarks ->
            val bookmarkedSet = bookmarks.map { "${it.surahId}:${it.ayahNumber}" }.toSet()
            ayahs.map {
                Ayah(
                    id = it.id,
                    surahId = it.surahId,
                    ayahNumber = it.ayahNumber,
                    textUthmani = it.textUthmani,
                    translationId = it.translationId,
                    juz = it.juz,
                    page = it.page,
                    manzil = it.manzil,
                    sajdah = (it.sajdah == 1),
                    isBookmarked = bookmarkedSet.contains("${it.surahId}:${it.ayahNumber}")
                )
            }
        }
    }

    override fun searchAyahs(query: String): Flow<List<Ayah>> {
        return ayahDao.searchAyahs(query).map { list ->
            list.map {
                Ayah(
                    id = it.id,
                    surahId = it.surahId,
                    ayahNumber = it.ayahNumber,
                    textUthmani = it.textUthmani,
                    translationId = it.translationId,
                    juz = it.juz,
                    page = it.page,
                    manzil = it.manzil,
                    sajdah = (it.sajdah == 1)
                )
            }
        }
    }
}

class AsmaulHusnaRepositoryImpl(
    private val asmaulHusnaDao: AsmaulHusnaDao
) : AsmaulHusnaRepository {
    override fun getAllAsmaulHusna(): Flow<List<AsmaulHusna>> {
        return asmaulHusnaDao.getAllAsmaulHusna().map { list ->
            list.map {
                AsmaulHusna(
                    number = it.number,
                    arabic = it.arabic,
                    latin = it.latin,
                    translationId = it.translationId,
                    meaningDetail = it.meaningDetail
                )
            }
        }
    }

    override fun searchAsmaulHusna(query: String): Flow<List<AsmaulHusna>> {
        return asmaulHusnaDao.searchAsmaulHusna(query).map { list ->
            list.map {
                AsmaulHusna(
                    number = it.number,
                    arabic = it.arabic,
                    latin = it.latin,
                    translationId = it.translationId,
                    meaningDetail = it.meaningDetail
                )
            }
        }
    }
}

class DoaRepositoryImpl(
    private val doaDao: DoaDao
) : DoaRepository {
    override fun getAllDoa(): Flow<List<Doa>> {
        return doaDao.getAllDoa().map { list ->
            list.map {
                Doa(
                    id = it.id,
                    category = it.category,
                    title = it.title,
                    arabic = it.arabic,
                    transliteration = it.transliteration,
                    translationId = it.translationId,
                    reference = it.reference
                )
            }
        }
    }

    override fun getDoaByCategory(category: String): Flow<List<Doa>> {
        return doaDao.getDoaByCategory(category).map { list ->
            list.map {
                Doa(
                    id = it.id,
                    category = it.category,
                    title = it.title,
                    arabic = it.arabic,
                    transliteration = it.transliteration,
                    translationId = it.translationId,
                    reference = it.reference
                )
            }
        }
    }

    override fun getCategories(): Flow<List<String>> {
        return doaDao.getCategories()
    }
}

class UserDataRepositoryImpl(
    private val userDataDao: UserDataDao
) : UserDataRepository {
    override fun getAllBookmarks(): Flow<List<Bookmark>> {
        return userDataDao.getAllBookmarks().map { list ->
            list.map {
                Bookmark(
                    id = it.id,
                    surahId = it.surahId,
                    ayahNumber = it.ayahNumber,
                    surahName = it.surahName,
                    textPreview = it.textPreview,
                    timestamp = it.timestamp
                )
            }
        }
    }

    override fun isBookmarked(surahId: Int, ayahNumber: Int): Flow<Boolean> {
        return userDataDao.isBookmarked(surahId, ayahNumber)
    }

    override suspend fun toggleBookmark(
        surahId: Int,
        ayahNumber: Int,
        surahName: String,
        textPreview: String
    ) {
        val isCurrent = userDataDao.isBookmarked(surahId, ayahNumber).first()
        if (isCurrent) {
            userDataDao.deleteBookmark(surahId, ayahNumber)
        } else {
            userDataDao.insertBookmark(
                BookmarkEntity(
                    surahId = surahId,
                    ayahNumber = ayahNumber,
                    surahName = surahName,
                    textPreview = textPreview
                )
            )
        }
    }

    override fun getLastRead(): Flow<LastRead?> {
        return userDataDao.getLastRead().map { entity ->
            entity?.let {
                LastRead(
                    surahId = it.surahId,
                    ayahNumber = it.ayahNumber,
                    surahName = it.surahName,
                    juz = it.juz,
                    timestamp = it.timestamp
                )
            }
        }
    }

    override suspend fun saveLastRead(
        surahId: Int,
        ayahNumber: Int,
        surahName: String,
        juz: Int
    ) {
        userDataDao.saveLastRead(
            LastReadEntity(
                surahId = surahId,
                ayahNumber = ayahNumber,
                surahName = surahName,
                juz = juz
            )
        )
    }
}

class QiblaRepositoryImpl(
    private val compassEngine: CompassEngine
) : QiblaRepository {

    // Default: Monas, Jakarta (-6.1754, 106.8272)
    private var userLat: Double = -6.1754
    private var userLng: Double = 106.8272

    private val _locationFlow = MutableStateFlow(Pair(userLat, userLng))

    override val qiblaState: Flow<QiblaData> = combine(
        compassEngine.compassState,
        _locationFlow
    ) { compassState, (lat, lng) ->
        val qiblaBearing = QiblaCalculator.calculateQiblaBearing(lat, lng)
        val needleRotation = QiblaCalculator.calculateNeedleRotation(qiblaBearing, compassState.trueHeading)
        val distanceKm = QiblaCalculator.calculateDistanceToKaaba(lat, lng)
        val isPointing = abs(needleRotation) < 2.5f

        QiblaData(
            qiblaBearing = qiblaBearing,
            currentHeading = compassState.trueHeading,
            needleRotation = needleRotation,
            distanceKm = distanceKm,
            accuracyStatus = compassState.accuracyStatus,
            needsCalibration = compassState.needsCalibration,
            isPointingToKaaba = isPointing
        )
    }

    override fun startCompass() {
        compassEngine.startListening()
    }

    override fun stopCompass() {
        compassEngine.stopListening()
    }

    override fun updateLocation(lat: Double, lng: Double) {
        userLat = lat
        userLng = lng
        compassEngine.updateLocation(lat, lng)
        _locationFlow.value = Pair(lat, lng)
    }
}
