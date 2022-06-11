package com.example.matchinggame.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.matchinggame.models.UserImageList
import com.example.matchinggame.repository.GameRepository

class GameViewModel(): ViewModel() {
    private val repository:GameRepository= GameRepository()
    private val _userImageLists=MutableLiveData<List<UserImageList>>()
    val userImageLists:LiveData<List<UserImageList>> =_userImageLists

    init {
        repository.fetchAllGames(_userImageLists)
    }
}