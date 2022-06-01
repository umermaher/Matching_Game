package com.example.matchinggame.models

import com.google.firebase.firestore.PropertyName

data class UserImageList(val name:String?=null,val images:List<String>?=null,val creator:String?=null)