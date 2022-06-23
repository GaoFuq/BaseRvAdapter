package com.gfq.baservadapter.refresh

/**
 *  2022/6/23 18:18
 * @auth gaofuq
 * @description
 */
interface OnStateChangeListener<T> {
    fun onStateChange(helper: RefreshHelper<T>,state: State)
}