package com.example.TODO.model

import android.content.SharedPreferences
import com.example.TODO.model.cases.Case
import com.example.TODO.model.habits.Habit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class GsonRepository(private val sharedPreferences: SharedPreferences, private val gson: Gson) {

    // Mutex to handle concurrent access to SharedPreferences
    private val mutex = Mutex()

    // For managing habits
    private val habitsFlow = MutableStateFlow<WorkResult<List<Habit>>>(WorkResult.SuccessResult(emptyList()))

    // For managing cases
    private val casesMap = mutableMapOf<Long, MutableStateFlow<WorkResult<List<Case>>>>()

    init {
        // Load initial data
        loadHabits()
    }

    // --- Habits Management ---
    fun getHabitsFlow(): Flow<WorkResult<List<Habit>>> = habitsFlow

    private fun loadHabits() {
        val habits = getHabits()
        habitsFlow.value = WorkResult.SuccessResult(habits)
    }
    suspend fun reloadHabits() {
        mutex.withLock {
            loadHabits() // Reload habits and update the flow
        }
    }
    suspend fun createHabit(habit: Habit) {
        mutex.withLock {
            val currentHabits = getHabits().toMutableList()
            val newHabit = habit.copy(id = generateHabitId(currentHabits))
            currentHabits.add(newHabit)
            saveHabits(currentHabits)
            habitsFlow.value = WorkResult.SuccessResult(currentHabits)
        }
    }

    suspend fun deleteHabit(habitId: Long) {
        mutex.withLock {
            val currentHabits = getHabits().filterNot { it.id == habitId }
            saveHabits(currentHabits)
            habitsFlow.value = WorkResult.SuccessResult(currentHabits)
            casesMap.remove(habitId)  // Remove associated cases
            sharedPreferences.edit().remove("cases_$habitId").apply()
        }
    }

    suspend fun refactorHabit(habitId: Long, newName: String) {
        mutex.withLock {
            val currentHabits = getHabits().toMutableList()
            val index = currentHabits.indexOfFirst { it.id == habitId }
            if (index != -1) {
                currentHabits[index] = currentHabits[index].copy(name = newName)
                saveHabits(currentHabits)
                habitsFlow.value = WorkResult.SuccessResult(currentHabits)
            }
        }
    }

    private fun generateHabitId(habits: List<Habit>): Long {
        return (habits.maxOfOrNull { it.id } ?: 0L) + 1
    }

    private fun getHabits(): List<Habit> {
        val jsonString = sharedPreferences.getString("habits", null)
        return if (!jsonString.isNullOrEmpty()) {
            val type = object : TypeToken<List<Habit>>() {}.type
            gson.fromJson(jsonString, type)
        } else emptyList()
    }

    private fun saveHabits(habits: List<Habit>) {
        val jsonString = gson.toJson(habits)
        sharedPreferences.edit().putString("habits", jsonString).apply()
    }

    // --- Cases Management ---
    fun getCasesFlow(habitId: Long): Flow<WorkResult<List<Case>>> {
        return casesMap.getOrPut(habitId) {
            MutableStateFlow(WorkResult.SuccessResult(getCases(habitId)))
        }
    }

    suspend fun createCase(habitId: Long, newCase: Case) {
        mutex.withLock {
            val currentCases = getCases(habitId).toMutableList()
            val caseWithId = newCase.copy(id = generateCaseId(currentCases))
            currentCases.add(caseWithId)
            saveCases(habitId, currentCases)
            casesMap[habitId]?.value = WorkResult.SuccessResult(currentCases)
        }
    }

    suspend fun deleteCase(habitId: Long, caseId: Long) {
        mutex.withLock {
            val currentCases = getCases(habitId).filterNot { it.id == caseId }
            saveCases(habitId, currentCases)
            casesMap[habitId]?.value = WorkResult.SuccessResult(currentCases)
        }
    }

    suspend fun updateCommentCase(habitId: Long, caseId: Long, newComment: String) {
        mutex.withLock {
            val currentCases = getCases(habitId).toMutableList()
            val index = currentCases.indexOfFirst { it.id == caseId }
            if (index != -1) {
                currentCases[index] = currentCases[index].copy(comment = newComment)
                saveCases(habitId, currentCases)
                casesMap[habitId]?.value = WorkResult.SuccessResult(currentCases)
            }
        }
    }

    private fun generateCaseId(cases: List<Case>): Long {
        return (cases.maxOfOrNull { it.id } ?: 0L) + 1
    }

    private fun getCases(habitId: Long): List<Case> {
        val jsonString = sharedPreferences.getString("cases_$habitId", null)
        return if (!jsonString.isNullOrEmpty()) {
            val type = object : TypeToken<List<Case>>() {}.type
            gson.fromJson(jsonString, type)
        } else emptyList()
    }

    private fun saveCases(habitId: Long, cases: List<Case>) {
        val jsonString = gson.toJson(cases)
        sharedPreferences.edit().putString("cases_$habitId", jsonString).apply()
    }
}
