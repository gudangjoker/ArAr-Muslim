package com.arar.muslim.presentation.quran

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arar.muslim.domain.model.Ayah
import com.arar.muslim.domain.model.Bookmark
import com.arar.muslim.domain.model.LastRead
import com.arar.muslim.domain.model.Surah
import com.arar.muslim.domain.repository.QuranRepository
import com.arar.muslim.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class QuranUiState(
    val surahs: List<Surah> = emptyList(),
    val searchQuery: String = "",
    val lastRead: LastRead? = null,
    val bookmarks: List<Bookmark> = emptyList(),
    val selectedSurah: Surah? = null,
    val ayahs: List<Ayah> = emptyList(),
    val selectedJuz: Int = 1,
    val juzAyahs: List<Ayah> = emptyList(),
    val isLoading: Boolean = false
)

class QuranViewModel(
    private val quranRepository: QuranRepository,
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedSurah = MutableStateFlow<Surah?>(null)
    val selectedSurah = _selectedSurah.asStateFlow()

    private val _selectedSurahAyahs = MutableStateFlow<List<Ayah>>(emptyList())
    val selectedSurahAyahs = _selectedSurahAyahs.asStateFlow()

    private val _selectedJuz = MutableStateFlow(1)
    val selectedJuz = _selectedJuz.asStateFlow()

    private val _juzAyahs = MutableStateFlow<List<Ayah>>(emptyList())
    val juzAyahs = _juzAyahs.asStateFlow()

    val surahsList: StateFlow<List<Surah>> = combine(
        quranRepository.getAllSurahs(),
        _searchQuery
    ) { surahs, query ->
        if (query.isBlank()) {
            surahs
        } else {
            surahs.filter {
                it.nameLatin.contains(query, ignoreCase = true) ||
                it.nameTranslation.contains(query, ignoreCase = true) ||
                it.number.toString() == query.trim()
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lastRead: StateFlow<LastRead?> = userDataRepository.getLastRead()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val bookmarks: StateFlow<List<Bookmark>> = userDataRepository.getAllBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun selectSurah(surahId: Int) {
        viewModelScope.launch {
            val surah = quranRepository.getSurahById(surahId)
            _selectedSurah.value = surah
            quranRepository.getAyahsBySurah(surahId).collect {
                _selectedSurahAyahs.value = it
            }
        }
    }

    fun selectJuz(juz: Int) {
        _selectedJuz.value = juz
        viewModelScope.launch {
            quranRepository.getAyahsByJuz(juz).collect {
                _juzAyahs.value = it
            }
        }
    }

    fun toggleBookmark(ayah: Ayah, surahName: String) {
        viewModelScope.launch {
            val preview = ayah.translationId.take(80) + if (ayah.translationId.length > 80) "..." else ""
            userDataRepository.toggleBookmark(
                surahId = ayah.surahId,
                ayahNumber = ayah.ayahNumber,
                surahName = surahName,
                textPreview = preview
            )
        }
    }

    fun saveLastRead(ayah: Ayah, surahName: String) {
        viewModelScope.launch {
            userDataRepository.saveLastRead(
                surahId = ayah.surahId,
                ayahNumber = ayah.ayahNumber,
                surahName = surahName,
                juz = ayah.juz
            )
        }
    }
}
