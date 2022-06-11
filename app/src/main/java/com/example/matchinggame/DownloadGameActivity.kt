package com.example.matchinggame

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.matchinggame.adapter.DownloadGameAdapter
import com.example.matchinggame.models.UserImageList
import com.example.matchinggame.utils.EXTRA_GAME_NAME
import com.example.matchinggame.utils.InternetConnectivityLiveData
import com.example.matchinggame.viewmodel.GameViewModel
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
    private lateinit var viewModel:GameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download_game)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title="Download Custom Game"

        val connectivityLiveData= InternetConnectivityLiveData(this)
        connectivityLiveData.observe(this) {
            if (it == false){
                applyMsgAnim(AnimationUtils.loadAnimation(this, R.anim.net_msg_from_top),View.VISIBLE)
            }
            else{
                applyMsgAnim( AnimationUtils.loadAnimation(this, R.anim.net_msg_to_top),View.GONE)
            }
        }

//        retrieveGames()
        gameAdapter= DownloadGameAdapter(this,createItemClickListener())
        rvDownload.adapter=gameAdapter

        viewModel=GameViewModel()
        viewModel.userImageLists.observe(this) {
            gameAdapter.updateList(it)
        }

        rvDownload.layoutManager= LinearLayoutManager(this)
        rvDownload.setHasFixedSize(true)
    }

    private fun applyMsgAnim(loadAnimation: Animation, visibility: Int) {
        noConnectionMessage.startAnimation(loadAnimation)
        noConnectionMessage.visibility= visibility
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==android.R.id.home){
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun createItemClickListener()= object :DownloadGameAdapter.OnItemClick {
        override fun onItemClicked(userImageList: UserImageList) {
            val name=userImageList.name
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

/****    old method  **/
//    private fun retrieveGames() {
//        db.collection("games").addSnapshotListener(EventListener<QuerySnapshot> { value, error ->
//
//            if(error!=null) return@EventListener
//
//            for(dc in value!!.documentChanges){
//
//                if(dc.type==DocumentChange.Type.ADDED){
//
//                    val userImageList=dc.document.toObject(UserImageList::class.java)
//                    Picasso.get().load(userImageList.images?.get(0)).fetch()
//                    (userImageLists as ArrayList<UserImageList>).add(userImageList)
//                    gameAdapter.notifyDataSetChanged()
//
//                }
//            }
//        })
//    }
}