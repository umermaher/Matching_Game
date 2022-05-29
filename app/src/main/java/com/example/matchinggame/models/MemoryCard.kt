package com.example.matchinggame.models

data class MemoryCard(val identifier: Int, var imageUrl:String?=null, var isFaceUp: Boolean=false, var isMatched:Boolean = false)