package com.tory.dmzj.comic.module.rank

import com.tory.library.base.VLayoutListFragment

/**
 * @author tory
 * @create 2020/9/4
 * @Describe
 */
class ComicRankFragment: VLayoutListFragment() {
    override fun registerViews() {
    }

    companion object {

        fun newFragment(): ComicRankFragment {
            return ComicRankFragment()
        }
    }
}