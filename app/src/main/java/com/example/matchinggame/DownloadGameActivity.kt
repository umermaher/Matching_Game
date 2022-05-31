package com.example.matchinggame

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.matchinggame.adapter.DownloadGameAdapter
import com.example.matchinggame.models.UserImageList
import com.example.matchinggame.utils.EXTRA_GAME_NAME
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.firebase.ui.firestore.SnapshotParser
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_download_game.*

class DownloadGameActivity : AppCompatActivity() {

    private lateinit var gameAdapter:DownloadGameAdapter
    private val db=Firebase.firestore
    private lateinit var userImageLists:List<UserImageList>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download_game)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title="Download Custom Game"

        userImageLists=ArrayList()

        retrieveGames()

        gameAdapter= DownloadGameAdapter(this,userImageLists,createItemClickListener())

        rvDownload.adapter=gameAdapter
        rvDownload.layoutManager= LinearLayoutManager(this)
        rvDownload.setHasFixedSize(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==android.R.id.home){
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun retrieveGames() {
        db.collection("games").addSnapshotListener(EventListener<QuerySnapshot> { value, error ->

            if(error!=null) return@EventListener

            for(dc in value!!.documentChanges){
                if(dc.type==DocumentChange.Type.ADDED){
                    val userImageList=dc.document.toObject(UserImageList::class.java)
                    Picasso.get().load(userImageList.images?.get(0)).fetch()
                    (userImageLists as ArrayList<UserImageList>).add(userImageList)
                    gameAdapter.notifyDataSetChanged()
                }
            }
        })
    }

    private fun createItemClickListener()= object :DownloadGameAdapter.OnItemClick {
        override fun onItemClicked(position: Int) {
            val name=userImageLists[position].name
            AlertDialog.Builder(this@DownloadGameActivity)
                .setTitle("Let's play '$name'")
                .setPositiveButton("Yes",DialogInterface.OnClickListener { _, _ ->
                    val resultData= Intent()
                    resultData.putExtra(EXTRA_GAME_NAME,name)
                    setResult(Activity.RESULT_OK,resultData)
                    finish()
                })
                .show()
        }
    }
}