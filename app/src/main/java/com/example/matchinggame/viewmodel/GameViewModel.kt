package com.example.matchinggame.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.matchinggame.models.UserImageList
import com.example.matchinggame.repository.GameRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameViewModel(): ViewModel() {
    private val repository:GameRepository= GameRepository()
    private val _userImageLists=MutableLiveData<List<UserImageList>>()
    val userImageLists:LiveData<List<UserImageList>> =_userImageLists

    init {
        fetchAllGames(_userImageLists)
    }

    private fun fetchAllGames(_userImageLists: MutableLiveData<List<UserImageList>>) =viewModelScope.launch {
        repository.fetchAllGames(_userImageLists)
    }

//    fun addGame(customGameName: String, userImageList: UserImageList)=viewModelScope.launch(Dispatchers.IO){
//        repository.addGameToFirestore(_doneUpload as MutableLiveData<Boolean>, customGameName,userImageList)
//    }
}