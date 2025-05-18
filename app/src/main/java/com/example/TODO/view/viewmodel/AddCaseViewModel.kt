package com.example.TODO.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.TODO.model.GsonRepository
import com.example.TODO.model.cases.Case
import com.example.TODO.view.Consts
import kotlinx.coroutines.launch

class AddCaseViewModel(
    private val repository: GsonRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var currentHabitId: Long? = null

    fun init(habitId: Long) {
        Log.d("AddCaseViewModel", "Initializing with habitId: $habitId")
        currentHabitId = habitId
        savedStateHandle[Consts.KEY_HABIT_ID] = habitId
    }

    fun createCase(case: Case) {
        currentHabitId?.let { habitId ->
            viewModelScope.launch {
                try {
                    Log.d("AddCaseViewModel", "Creating case: $case for habit ID: $habitId")
                    repository.createCase(habitId, case)
                } catch (e: Exception) {
                    Log.e("AddCaseViewModel", "Error creating case", e)
                }
            }
        } ?: run {
            Log.e("AddCaseViewModel", "Habit ID is null when trying to create a case")
        }
    }
}
