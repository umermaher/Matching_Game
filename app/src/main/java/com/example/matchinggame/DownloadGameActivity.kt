package com.example.matchinggame

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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

        db.collection("games").addSnapshotListener(object: EventListener<QuerySnapshot> {
            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                for(dc in value!!.documentChanges){
                    if(dc.type==DocumentChange.Type.ADDED){
                        val userImageList=dc.document.toObject(UserImageList::class.java)
                        (userImageLists as ArrayList<UserImageList>).add(userImageList)
                        gameAdapter.notifyDataSetChanged()
                        Toast.makeText(this@DownloadGameActivity,"${userImageList.name}",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })

        gameAdapter= DownloadGameAdapter(this,userImageLists,createItemClickListener())

        rvDownload.adapter=gameAdapter
        rvDownload.layoutManager= LinearLayoutManager(this)
        rvDownload.setHasFixedSize(true)
    }

    private fun createItemClickListener()= object :DownloadGameAdapter.OnItemClick {
        override fun onItemClicked(position: Int) {
            AlertDialog.Builder(this@DownloadGameActivity)
                .setTitle("Let's play")
                .setPositiveButton("Yes",DialogInterface.OnClickListener { _, _ ->
//                    val resultData= Intent()
//                    resultData.putExtra(EXTRA_GAME_NAME,name)
//                    setResult(Activity.RESULT_OK,resultData)
//                    finish()
                })
                .show()
        }

    }
}