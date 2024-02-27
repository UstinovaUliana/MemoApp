package com.example.memoapp.helper

import android.content.Context
import android.content.SharedPreferences
import com.example.memoapp.model.Level

const val LEVEL = "StoredLevel"
const val PREFS_NAME = "MemoAppSharedPrefs"

class SharedPrefs(context: Context) {

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun storeLevel(level: Level) {
        sharedPreferences.edit().putInt(LEVEL, level.numberOfCards).apply()
    }

    fun getStoredLevel() = sharedPreferences.getInt(LEVEL, Level.BEGINNER.numberOfCards)
}