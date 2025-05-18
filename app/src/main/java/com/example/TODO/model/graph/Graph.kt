package com.example.TODO.model.graph

import com.example.TODO.view.adapters.ItemList
import java.util.Date

data class Graph(
    val data: MutableMap<Date, Int>
) : ItemList