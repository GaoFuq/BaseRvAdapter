package com.gfq.baservadapter.refresh

import android.content.Context
import android.view.LayoutInflater
import android.view.View

/**
 *  2022/2/11 13:36
 * @auth gaofuq
 * @description
 */
interface  IStateView {
    fun refreshLoadingView(context: Context, helper: RefreshHelper<*>): View?=null
    fun refreshSuccessView(context: Context, helper: RefreshHelper<*>): View?=null
    fun refreshErrorView(context: Context, helper: RefreshHelper<*>): View?=null

    fun loadMoreLoadingView(context: Context, helper: RefreshHelper<*>): View?=null
    fun loadMoreSuccessView(context: Context, helper: RefreshHelper<*>): View?=null
    fun loadMoreErrorView(context: Context, helper: RefreshHelper<*>): View?=null

    /**
     * 列表没有数据时显示的View
     */
    fun emptyDataView(context: Context, helper: RefreshHelper<*>): View?=null
    /**
     * 当前刷新请求返回的数据为null或empty时显示的View
     */
    fun emptyDataWithRefreshView(context: Context, helper: RefreshHelper<*>): View?=null
    /**
     * 当前加载更多请求返回的数据为null或empty时显示的View
     */
    fun emptyDataWithLoadMoreView(context: Context, helper: RefreshHelper<*>): View?=null

    fun netLoseView(context:Context,helper:RefreshHelper<*>):View?=null

}
