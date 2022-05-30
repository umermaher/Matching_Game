package com.example.matchinggame.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.example.matchinggame.R
import com.example.matchinggame.models.IntroScreenItem
import kotlinx.android.synthetic.main.pager_layout.view.*

class IntroViewPagerAdapter (private val context:Context,private val itemList:List<IntroScreenItem>): PagerAdapter() {

    @SuppressLint("InflateParams")
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val layoutInflater=context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        //screen layout is actually a pager layout
        val screenLayout=layoutInflater.inflate(R.layout.pager_layout,null)
        val item= itemList[position]
        screenLayout.introImage.setImageResource(item.identifier)
        screenLayout.introTitle.text=item.title
        screenLayout.introDesription.text=item.description

        container.addView(screenLayout)

        return screenLayout
    }

    override fun getCount(): Int = itemList.size

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) = container.removeView(`object` as View)

    override fun isViewFromObject(view: View, `object`: Any): Boolean = view==`object`
}