package com.arar.muslim.presentation.asmaulhusna

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arar.muslim.domain.model.AsmaulHusna
import com.arar.muslim.domain.repository.AsmaulHusnaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class AsmaulHusnaViewModel(
    private val repository: AsmaulHusnaRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedName = MutableStateFlow<AsmaulHusna?>(null)
    val selectedName = _selectedName.asStateFlow()

    val namesList: StateFlow<List<AsmaulHusna>> = combine(
        repository.getAllAsmaulHusna(),
        _searchQuery
    ) { names, query ->
        if (query.isBlank()) {
            names
        } else {
            names.filter {
                it.latin.contains(query, ignoreCase = true) ||
                it.translationId.contains(query, ignoreCase = true) ||
                it.number.toString() == query.trim()
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun selectName(name: AsmaulHusna?) {
        _selectedName.value = name
    }
}
