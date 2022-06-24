package com.gfq.baservadapter

import androidx.recyclerview.widget.PagerSnapHelper

/**
 *  2022/6/24 10:52
 * @auth gaofuq
 * @description
 */
class PagerSnapHelperZ :PagerSnapHelper() {
    override fun onFling(velocityX: Int, velocityY: Int): Boolean {
        super.onFling(velocityX, velocityY)
        return false
    }
}