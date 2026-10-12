package com.arar.muslim.presentation.doa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arar.muslim.domain.model.Doa
import com.arar.muslim.domain.repository.DoaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class DzikirViewModel(
    private val doaRepository: DoaRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow("Pagi-Petang")
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _filterKeyword = MutableStateFlow("")
    val filterKeyword = _filterKeyword.asStateFlow()

    // Counter state per doa ID
    private val _counters = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val counters = _counters.asStateFlow()

    val doaList: StateFlow<List<Doa>> = combine(
        doaRepository.getAllDoa(),
        _selectedCategory,
        _filterKeyword
    ) { allDoa, category, query ->
        val filteredByCategory = if (category == "Semua") {
            allDoa
        } else {
            allDoa.filter { it.category == category }
        }

        if (query.isBlank()) {
            filteredByCategory
        } else {
            filteredByCategory.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.translationId.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun onSearchQueryChanged(query: String) {
        _filterKeyword.value = query
    }

    fun incrementCounter(doaId: Int, maxTarget: Int = 100) {
        val current = _counters.value[doaId] ?: 0
        if (current < maxTarget) {
            _counters.value = _counters.value.toMutableMap().apply {
                this[doaId] = current + 1
            }
        }
    }

    fun resetCounter(doaId: Int) {
        _counters.value = _counters.value.toMutableMap().apply {
            this[doaId] = 0
        }
    }
}
