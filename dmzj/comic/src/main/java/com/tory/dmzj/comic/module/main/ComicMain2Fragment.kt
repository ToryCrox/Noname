package com.tory.dmzj.comic.module.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.alibaba.android.arouter.facade.annotation.Route
import com.tory.dmzj.comic.R
import com.tory.dmzj.comic.module.rank.ComicRankFragment
import com.tory.dmzj.comic.module.recommend.ComicRecommendFragment
import com.tory.dmzj.dbase.RouterTable
import com.tory.library.base.BaseFragment
import com.tory.library.log.LogUtils
import kotlinx.android.synthetic.main.fragment_comic_main2.*


/**
 * Author: tory
 * Date: 2020/10/16
 * Email: xutao@theduapp.com
 * Description:
 */
@Route(path = RouterTable.COMIC_MAIN_PAGE)
class ComicMain2Fragment: BaseFragment() {


    override fun getLayoutId(): Int = R.layout.fragment_comic_main2

    override fun initView(view: View, savedInstanceState: Bundle?) {
        val pagerAdapter = ComicFragmentAdapter(this)
        viewPager.adapter = pagerAdapter
        viewPager.setCurrentItem(1, false)
        tabLayout.setupWithViewPager2(viewPager, {tab, position->
            tab.setText(getPageTitle(position))
        })
    }

    fun getPageTitle(position: Int): CharSequence? {
        return when(position) {
            1 -> "排行"
            else -> "推荐"
        }
    }

    class ComicFragmentAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int {
            return 4
        }

        override fun createFragment(position: Int): Fragment {
            LogUtils.d("createFragment position:$position")
            return if (position == 1) {
                ComicRankFragment.newFragment()
            } else {
                ComicRecommendFragment.newInstance()
            }
        }
    }

}