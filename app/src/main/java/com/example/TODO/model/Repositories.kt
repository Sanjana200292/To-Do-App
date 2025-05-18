package com.example.TODO.model

import android.content.Context
import com.google.gson.Gson

object Repositories {

    lateinit var gsonRepository: GsonRepository

    fun init(context: Context) {
        // Initialize Gson and SharedPreferences here
        val gson = Gson()
        val sharedPreferences = context.getSharedPreferences("your_shared_prefs", Context.MODE_PRIVATE)

        gsonRepository = GsonRepository(sharedPreferences, gson)
    }
}

