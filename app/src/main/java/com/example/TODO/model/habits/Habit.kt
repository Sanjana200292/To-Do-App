package com.example.TODO.model.habits

import java.time.LocalTime

data class Habit(
    val id: Long,
    var name: String,
    val startTime: LocalTime? = null
)