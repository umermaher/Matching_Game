package com.example.matchinggame.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView
import com.example.matchinggame.R
import com.example.matchinggame.models.BoardSize
import com.example.matchinggame.models.MemoryCard
import com.squareup.picasso.Picasso
import kotlin.math.min

class MemoryCardAdapter(
    private val context: Context,
    private val boardSize: BoardSize,
    private val memoryCards: List<MemoryCard>,
    private val listener : OnCardClickListener
) : RecyclerView.Adapter<MemoryCardAdapter.ViewHolder>() {

    companion object{
        private const val MARGIN_SIZE=10
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val cardWidth = parent.width / boardSize.getWidth() - 2 * MARGIN_SIZE
        val cardHeight = parent.height / boardSize.getHeight() - 2 * MARGIN_SIZE
        val cardSideLength = min(cardWidth, cardHeight)
        val view = LayoutInflater.from(context).inflate(R.layout.memory_card, parent, false)
        val layoutParams = view.findViewById<CardView>(R.id.cardView).layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.width = cardSideLength
        layoutParams.height = cardSideLength
        layoutParams.setMargins(MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount() : Int = boardSize.numCards

    interface OnCardClickListener{
        fun onCardClicked(position: Int)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private val imageBtn:ImageButton=itemView.findViewById(R.id.imageBtn)

        fun bind(position: Int) {
            val card= memoryCards[position]

            if(card.isFaceUp){
                if(card.imageUrl!=null){
                    Picasso.get().load(card.imageUrl).into(imageBtn)
                }else{
                    if(card.identifier==R.drawable.ic_apple){
                        imageBtn.setPadding(50)
                    }
                    imageBtn.setImageResource(card.identifier)
                }
            }else {
                imageBtn.setPadding(0)
                imageBtn.setImageResource(R.color.theme_500)
            }
//            imageBtn.setImageResource( if(card.isFaceUp) card.identifier else R.color.theme_500)

            imageBtn.alpha=if(card.isMatched) .5f else 1.0f
            val colorStateList=if(card.isMatched) ContextCompat.getColorStateList(context,
                R.color.gray
            ) else null
            ViewCompat.setBackgroundTintList(imageBtn,colorStateList)

            imageBtn.setOnClickListener {
                listener.onCardClicked(position)
            }
        }
    }
}
