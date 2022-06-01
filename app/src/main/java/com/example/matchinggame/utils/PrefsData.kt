package com.example.matchinggame.utils

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.example.matchinggame.models.BoardSize

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

    fun getBest(context: Context, boardSize: BoardSize): Int {
        val sp=context.getSharedPreferences("myData", AppCompatActivity.MODE_PRIVATE)

        return when(boardSize){
            BoardSize.EASY -> sp.getInt(EASY_BEST,0)
            BoardSize.MEDIUM -> sp.getInt(MED_BEST,0)
            BoardSize.HARD -> sp.getInt(HARD_BEST,0)
        }
    }

    fun setBest(context: Context, boardSize: BoardSize,best:Int){
        val sp=context.getSharedPreferences("myData", AppCompatActivity.MODE_PRIVATE)
        val editor=sp.edit()

        //best are number of moves
        when(boardSize){
            BoardSize.EASY -> editor.putInt(EASY_BEST,best)
            BoardSize.MEDIUM -> editor.putInt(MED_BEST,best)
            BoardSize.HARD -> editor.putInt(HARD_BEST,best)
        }
        editor.apply()
    }

    fun isFirstTime(context: Context, boardSize: BoardSize):Boolean{
        val sp=context.getSharedPreferences("myData", AppCompatActivity.MODE_PRIVATE)

        return when(boardSize){
            BoardSize.EASY -> sp.getBoolean(EASY_FIRST,true)
            BoardSize.MEDIUM -> sp.getBoolean(MED_FIRST,true)
            BoardSize.HARD -> sp.getBoolean(HARD_FIRST,true)
        }
    }

    fun notFirstTime(context: Context, boardSize: BoardSize){
        val sp=context.getSharedPreferences("myData", AppCompatActivity.MODE_PRIVATE)
        val editor=sp.edit()
        when(boardSize){
            BoardSize.EASY -> editor.putBoolean(EASY_FIRST,false)
            BoardSize.MEDIUM -> editor.putBoolean(MED_FIRST,false)
            BoardSize.HARD -> editor.putBoolean(HARD_FIRST,false)
        }
        editor.apply()
    }
}