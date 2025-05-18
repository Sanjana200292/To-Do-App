package com.example.TODO.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.TODO.model.GsonRepository
import com.example.TODO.model.WorkResult
import com.example.TODO.model.cases.Case
import com.example.TODO.view.Consts.KEY_HABIT_ID
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CasesViewModel(
    private val repository: GsonRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var currentHabitId: Long? = savedStateHandle[KEY_HABIT_ID]
    private lateinit var cases: Flow<WorkResult<List<Case>>>
    private val numLoadingItems = MutableStateFlow(0)

    init {
        currentHabitId?.let {
            cases = repository.getCasesFlow(it)
        }
    }

    fun init(habitId: Long) {
        cases = repository.getCasesFlow(habitId)
        savedStateHandle[KEY_HABIT_ID] = habitId
        currentHabitId = habitId
    }

    val uiCasesState by lazy {
        combine(cases, numLoadingItems) { casesResult, loadingItems ->
            when (casesResult) {
                is WorkResult.SuccessResult -> CasesListUiState(
                    cases = casesResult.data.sortedByDescending { it.date },
                    isLoading = loadingItems > 0
                )
                is WorkResult.LoadingResult -> CasesListUiState(isLoading = true)
                is WorkResult.ErrorResult -> CasesListUiState(isError = true)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = CasesListUiState(isLoading = true)
        )
    }

    fun addCase(newCase: Case) {
        currentHabitId?.let { habitId ->
            viewModelScope.launch {
                withLoading {
                    repository.createCase(habitId, newCase)
                }
            }
        }
    }

    fun updateComment(case: Case, newComment: String) {
        currentHabitId?.let { habitId ->
            viewModelScope.launch {
                withLoading {
                    repository.updateCommentCase(habitId, case.id, newComment)
                }
            }
        }
    }

    fun deleteCase(case: Case) {
        currentHabitId?.let { habitId ->
            viewModelScope.launch {
                withLoading {
                    repository.deleteCase(habitId, case.id)
                }
            }
        }
    }

    private suspend fun withLoading(block: suspend () -> Unit) {
        try {
            numLoadingItems.value += 1
            block()
        } finally {
            numLoadingItems.value -= 1
        }
    }
}

data class CasesListUiState(
    val cases: List<Case> = emptyList(),
    val isLoading: Boolean = false,
    val isError: Boolean = false
)
