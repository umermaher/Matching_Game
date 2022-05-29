package com.example.matchinggame.models

import com.example.matchinggame.utils.DEFAULT_ICONS

class MemoryGame(private val boardSize: BoardSize, private val customGameImages: List<String>?) {
    val cards : List<MemoryCard>
    var numPairsFound=0
    private var numOfCardFlips=0
    private var indexOfSingleSelectedCard: Int?=null

    init {
        cards = if(customGameImages==null){
            val chosenImages= DEFAULT_ICONS.shuffled().take(boardSize.getNumPairs())
            val randomizedImages = (chosenImages+chosenImages).shuffled()
            randomizedImages.map { MemoryCard(it) }
        }else{
            val randomizedImages=(customGameImages+customGameImages).shuffled()
            randomizedImages.map { MemoryCard(it.hashCode(),it) }
        }
    }
    fun flipCard(position: Int) : Boolean{
        val card=cards[position]
        numOfCardFlips++
        var foundMatch=false
        //Flipping card has three cases:
//        0 cards previously flipped over: restore card(as the one card is going to flip then restore card has no operation) + flip over the selected card.
//        1 card previously flipped over: flip over the selected card + check if the images matched
//        2 cards previously flipped over:  restore card + flip over the selected card.
        if(indexOfSingleSelectedCard==null){
            // 0 or 2 cards previously flipped over.
            restoreCards()
            indexOfSingleSelectedCard=position
        }else{
            //exactly 1 card previously flipped over.
            foundMatch = checkForMatch(indexOfSingleSelectedCard!!,position)
            indexOfSingleSelectedCard=null
        }

        // On case 1 & 3 : current card must be flip over
        // On case 2 : whether card is flipped or not due to below condition, it state will depend on the isMatch boolean value
        // that notify change in MemoryCardAdapter class.
        card.isFaceUp=!card.isFaceUp
        return foundMatch
    }

    private fun checkForMatch(position: Int, position1: Int): Boolean {
        if(cards[position].identifier == cards[position1].identifier){
            cards[position].isMatched=true
            cards[position1].isMatched=true
            numPairsFound++
            return true
        }
        return false
    }

    // Turn all unmatched cards face down
    private fun restoreCards() {
        for (card in cards) {
            if (!card.isMatched) {
                card.isFaceUp = false
            }
        }
    }

    fun haveWonGame() : Boolean = numPairsFound==boardSize.getNumPairs()

    fun isCardFaceUp(position: Int) : Boolean = cards[position].isFaceUp

    fun getNumMoves() : Int = numOfCardFlips/2
}
