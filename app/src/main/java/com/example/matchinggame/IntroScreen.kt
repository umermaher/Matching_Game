package com.example.matchinggame

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.core.view.WindowCompat
import androidx.viewpager.widget.ViewPager
import com.example.matchinggame.adapter.IntroViewPagerAdapter
import com.example.matchinggame.models.IntroScreenItem
import com.example.matchinggame.utils.PrefsData
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_intro_screen.*
import kotlinx.coroutines.*

class IntroScreen : AppCompatActivity() {
    private lateinit var introPagerAdapter: IntroViewPagerAdapter
    private var position=0
    private val list = mutableListOf<IntroScreenItem>()
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //for removing statusbar
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN)

        supportActionBar?.hide()

        setContentView(R.layout.activity_intro_screen)

        list.add(IntroScreenItem(R.drawable.ic_memory,"Test your memory",getString(R.string.firstText)))
        list.add(IntroScreenItem(R.drawable.ic_build,"Build your own game",getString(R.string.secondText)))
        list.add(IntroScreenItem(R.drawable.ic_download,"Play more games",getString(R.string.thirdText)))

        introPagerAdapter= IntroViewPagerAdapter(this,list)
        introPager.adapter=introPagerAdapter

        tabIndicator.setupWithViewPager(introPager)

        nextBtn.setOnClickListener {
            position=introPager.currentItem
            if(position<list.size){
                position++
                introPager.currentItem=position
                if(position==list.size-1){
                    //when we reach to the last intro page
                    // TODO: show the let's play button
                    loadLastScreen()
                }
            }
        }

        tabIndicator.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if(tab?.position==list.size-1)
                    loadLastScreen()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        playBtn.setOnClickListener {
            introPBar.visibility=View.VISIBLE
            val i=Intent(this,MainActivity::class.java)
            GlobalScope.launch {
                PrefsData.savePrefsData(this@IntroScreen)
                withContext(Dispatchers.Main){
                    introPBar.visibility=View.GONE
                    startActivity(i)
                    finish()
                }
            }
        }
    }

    private fun loadLastScreen() {
        nextBtn.visibility= View.INVISIBLE
        tabIndicator.visibility=View.INVISIBLE
        playBtn.visibility=View.VISIBLE

        playBtn.animation= AnimationUtils.loadAnimation(this,R.anim.play_btn_anim)
    }
}