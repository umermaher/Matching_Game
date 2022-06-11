package com.example.matchinggame.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.example.matchinggame.models.UserImageList
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class GameRepository {

    private val db= Firebase.firestore

    fun fetchAllGames(liveData: MutableLiveData<List<UserImageList>>) {
        db.collection("games").addSnapshotListener(EventListener<QuerySnapshot> { value, error ->

            if(error!=null) return@EventListener

            val userImageLists:List<UserImageList> = ArrayList()

            for(dc in value!!.documentChanges){

                if(dc.type== DocumentChange.Type.ADDED){

                    val userImageList=dc.document.toObject(UserImageList::class.java)
                    Picasso.get().load(userImageList.images?.get(0)).fetch()
                    (userImageLists as ArrayList<UserImageList>).add(userImageList)
                }
            }
            liveData.postValue(userImageLists)
        })
    }
}