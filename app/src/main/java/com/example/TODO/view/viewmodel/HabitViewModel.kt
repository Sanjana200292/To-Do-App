package com.example.TODO.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.TODO.model.GsonRepository
import com.example.TODO.model.WorkResult
import com.example.TODO.model.habits.Habit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HabitViewModel(
    private val repository: GsonRepository
) : ViewModel() {

    private val habitsFlow = repository.getHabitsFlow() // Flow of habits from the repository
    private val numLoadingItems = MutableStateFlow(0)

    val uiHabitsState = combine(habitsFlow, numLoadingItems) { habits, loadingItems ->
        when (habits) {
            is WorkResult.SuccessResult -> HabitsListUiState(
                habits = habits.data,
                isLoading = loadingItems > 0
            )
            is WorkResult.LoadingResult -> HabitsListUiState(isLoading = true)
            is WorkResult.ErrorResult -> HabitsListUiState(isError = true)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HabitsListUiState(isLoading = true)
    )

    // Method to update habits
    fun updateHabits() {
        viewModelScope.launch {
            repository.reloadHabits()
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            withLoading {
                repository.deleteHabit(habit.id)
                repository.reloadHabits()
            }
        }
    }

    fun renameHabit(id: Long, newName: String) {
        viewModelScope.launch {
            withLoading {
                repository.refactorHabit(id, newName)
                repository.reloadHabits()
            }
        }
    }

    fun addHabit(name: String) {
        viewModelScope.launch {
            withLoading {
                repository.createHabit(Habit(0, name))
                repository.reloadHabits()
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

data class HabitsListUiState(
    val habits: List<Habit> = emptyList(),
    val isLoading: Boolean = false,
    val isError: Boolean = false
)
