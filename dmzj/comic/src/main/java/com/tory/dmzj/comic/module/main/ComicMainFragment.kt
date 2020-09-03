package com.tory.dmzj.comic.module.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.tory.dmzj.comic.R
import com.tory.dmzj.comic.module.rank.ComicRankFragment
import com.tory.dmzj.comic.module.recommend.ComicRecommendFragment
import com.tory.dmzj.dbase.RouterTable
import com.tory.library.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_comic_main.*

/**
 * @author tory
 * @create 2020/9/4
 * @Describe
 */
@Route(path = RouterTable.COMIC_MAIN_PAGE)
class ComicMainFragment: BaseFragment() {

    override fun getLayoutId(): Int = R.layout.fragment_comic_main

    override fun initView(view: View, savedInstanceState: Bundle?) {
        val pagerAdapter = ComicFragmentAdapter(childFragmentManager)
        viewPager.adapter = pagerAdapter
        tabLayout.setupWithViewPager(viewPager)

        ivSearch.setOnClickListener {
            ARouter.getInstance()
                    .build(RouterTable.COMIC_SEARCH_PAGE)
                    .navigation(requireContext())
        }
    }


    class ComicFragmentAdapter(fm: FragmentManager)
        : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getItem(position: Int): Fragment {
            return if (position == 1) {
                ComicRankFragment.newFragment()
            } else {
                ComicRecommendFragment.newInstance()
            }
        }

        override fun getCount(): Int = 2

        override fun getPageTitle(position: Int): CharSequence? {
            return when(position) {
                1 -> "排行"
                else -> "推荐"
            }
        }
    }
}