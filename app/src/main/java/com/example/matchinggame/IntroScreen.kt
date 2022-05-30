package com.example.matchinggame

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.viewpager.widget.ViewPager
import com.example.matchinggame.adapter.IntroViewPagerAdapter
import com.example.matchinggame.models.IntroScreenItem
import kotlinx.android.synthetic.main.activity_intro_screen.*

class IntroScreen : AppCompatActivity() {
    private lateinit var introPagerAdapter: IntroViewPagerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro_screen)
        //for removing statusbar
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        supportActionBar?.hide()

        val list = mutableListOf<IntroScreenItem>()
        list.add(IntroScreenItem(R.drawable.ic_memory,"Test your memory","Description"))
        list.add(IntroScreenItem(R.drawable.ic_build,"Build your own game","Description"))
        list.add(IntroScreenItem(R.drawable.ic_download,"Play more games","Description"))

        introPagerAdapter= IntroViewPagerAdapter(this,list)
        introPager.adapter=introPagerAdapter
    }
}