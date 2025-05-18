package com.example.TODO.model.cases

import com.example.TODO.view.adapters.ItemList


data class Case (
    val id: Long,
    var comment: String,
    var date: Long,
    val habitId: Long
) : ItemList