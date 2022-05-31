package com.example.matchinggame.utils

import android.content.Context
import androidx.appcompat.app.AppCompatActivity

object PrefsData {
    fun restorePrefsData(context: Context): Boolean {
        val sp=context.getSharedPreferences("myData", AppCompatActivity.MODE_PRIVATE)
        return sp.getBoolean("isIntroOpened",false)
    }
    fun savePrefsData(context: Context) {
        val sp=context.getSharedPreferences("myData", AppCompatActivity.MODE_PRIVATE)
        val editor=sp.edit()
        editor.putBoolean("isIntroOpened",true)
        editor.apply()
    }
}