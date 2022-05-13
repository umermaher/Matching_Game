package com.example.matchinggame

import android.app.ActionBar
import android.content.Context
import android.net.Uri
import android.view.*
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.matchinggame.models.BoardSize
import kotlin.math.min

class ImagePickerAdapter(
    private val context: Context,
    private val imageUris: MutableList<Uri>,
    private val boardSize: BoardSize,
    private val onClickListener: OnImageClickListener,
    private val onRemoveClickListener: OnRemoveClickListener
) : RecyclerView.Adapter<ImagePickerAdapter.ViewHolder>() {

    companion object{
        private const val MARGIN_SIZE=10
    }

    override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : ImagePickerAdapter.ViewHolder {
        val cardWidth = parent.width / boardSize.getWidth() - (2* MARGIN_SIZE)
        val cardHeight = parent.height / boardSize.getHeight() - (2* MARGIN_SIZE)
        val cardSizeLength=min(cardWidth, cardHeight)

        val view=LayoutInflater.from(context).inflate(R.layout.image_card,null)
        val layoutParams=view.findViewById<CardView>(R.id.imageCardView).layoutParams as ViewGroup.MarginLayoutParams

        layoutParams.width=cardSizeLength
        layoutParams.height=cardSizeLength
        layoutParams.setMargins(MARGIN_SIZE,MARGIN_SIZE,MARGIN_SIZE,MARGIN_SIZE)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(position<imageUris.size) holder.bind(position)
        else holder.bind()
    }

    override fun getItemCount(): Int = boardSize.getNumPairs()

    interface OnImageClickListener{
        fun onPlaceHolderClicked()
    }

    interface OnRemoveClickListener{
        fun onRemoveClicked(position: Int)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnCreateContextMenuListener
    ,MenuItem.OnMenuItemClickListener{
        private val imageView=itemView.findViewById<ImageView>(R.id.imageToPick)
        fun bind() {
            imageView.setImageURI(null)
            imageView.setOnClickListener {
                onClickListener.onPlaceHolderClicked()
            }
        }
        fun bind(position: Int){
            imageView.setImageURI(imageUris[position])
            imageView.setOnClickListener(null)
            imageView.setOnCreateContextMenuListener(this)
        }

        override fun onCreateContextMenu(
            menu: ContextMenu?,
            view: View?,
            contextMenu: ContextMenu.ContextMenuInfo?
        ) {
            menu?.setHeaderTitle("Select Action")
            val item:MenuItem?=menu?.add(Menu.NONE,1,1,"Remove")
            item?.setOnMenuItemClickListener(this)
        }

        override fun onMenuItemClick(p0: MenuItem?): Boolean {
            if(onRemoveClickListener!=null){
                val position=adapterPosition
                if(position!=RecyclerView.NO_POSITION){
                    when(p0?.itemId){
                        1->{
                            onRemoveClickListener.onRemoveClicked(position)
                            return true
                        }
                    }
                }
            }
            return false
        }
    }
}
