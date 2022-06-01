package com.example.matchinggame.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.matchinggame.R
import com.example.matchinggame.models.UserImageList
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.download_game_card.view.*

class DownloadGameAdapter(
    private val context: Context,
    private val userImageLists: List<UserImageList>,
    private val onItemClick: OnItemClick) :
    RecyclerView.Adapter<DownloadGameAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view=LayoutInflater.from(context).inflate(R.layout.download_game_card,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = userImageLists.size

    interface OnItemClick{
        fun onItemClicked(position: Int)
    }

    inner class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        private var gameImage:ImageView=itemView.findViewById(R.id.gameImageView)
        private var gameName:TextView=itemView.findViewById(R.id.gameNameText)
        private var gameDif:TextView=itemView.findViewById(R.id.gameDifText)
        init {
            itemView.setOnClickListener {
                onItemClick.onItemClicked(adapterPosition)
            }
        }

        fun bind(position: Int) {
            val model=userImageLists[position]
            gameName.text=model.name
            gameDif.text = when(model.images?.size){
                4 -> "EASY"
                9 -> "MEDIUM"
                else -> {
                    "HARD"
                }
            }
//            Glide.with(context).load(model.images?.get(0)).centerCrop().into(gameImage)
            Picasso.get().load(model.images?.get(0)).placeholder(R.drawable.ic_image).into(gameImage)
        }
    }
}