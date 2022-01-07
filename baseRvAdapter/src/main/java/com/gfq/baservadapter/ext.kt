package com.gfq.baservadapter

import android.util.Log
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil.setContentView
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.scwang.smart.refresh.layout.SmartRefreshLayout


/**
 *  2021/6/2 14:41
 * @auth gaofuq
 * @description
 */
/**
 * 继承 RVSelectBean 使其具有 select 属性
 */
open class RVSelectBean(open var select: Boolean = false)

data class ViewTypeWrapper(@LayoutRes val viewTypeLayout: Int, val viewType: Int)

inline fun <reified T : ViewDataBinding> BaseVH.get(): T {
    return this.vhBinding as T
}


inline fun <reified T> FragmentActivity.createRefreshHelper(
    itemLayoutId: Int,
    refreshContainerView: ViewGroup? = null,
    crossinline onAdapterBindView: (holder: BaseVH, data: T, position: Int,adapter:BaseRVAdapter<T>) -> Unit,
    noinline request: (curPage: Int, pageDataCount: Int, callback: (List<T>?) -> Unit) -> Unit,
    noinline onRefreshStateChange: ((state: RefreshHelper.RefreshState) -> Boolean)? = null
): RefreshHelper<T> {
    return RefreshHelper(
        activityOrFragment = this,
        smartRefreshLayout = SmartRefreshLayout(this),
        recyclerView = RecyclerView(this),
        adapter = object : BaseRVAdapter<T>(itemLayoutId) {
            override fun onBindView(holder: BaseVH, data: T, position: Int) {
                onAdapterBindView(holder, data, position,this)
            }
        },
        requestData = request,
        onRefreshStateChange = {
            Log.e("RefreshHelper", "onRefreshStateChange " + it.name)
            onRefreshStateChange?.invoke(it) ?: false
        },
        isAutoCreate = true
    ).apply {
        refreshContainerView?.addView(smartRefreshLayout)
    }
}


inline fun <reified T> Fragment.createRefreshHelper(
    itemLayoutId: Int,
    refreshContainerView: ViewGroup? = null,
    crossinline onAdapterBindView: (holder: BaseVH, data: T, position: Int,adapter:BaseRVAdapter<T>) -> Unit,
    noinline request: (curPage: Int, pageDataCount: Int, callback: (List<T>?) -> Unit) -> Unit,
    noinline onRefreshStateChange: ((state: RefreshHelper.RefreshState) -> Boolean)? = null
): RefreshHelper<T> {
    return RefreshHelper(
        activityOrFragment = this,
        smartRefreshLayout = SmartRefreshLayout(context),
        recyclerView = RecyclerView(context!!),
        adapter = object : BaseRVAdapter<T>(itemLayoutId) {
            override fun onBindView(holder: BaseVH, data: T, position: Int) {
                onAdapterBindView(holder, data, position,this)
            }
        },
        requestData = request,
        onRefreshStateChange = {
            Log.e("RefreshHelper", "onRefreshStateChange " + it.name)
            onRefreshStateChange?.invoke(it) ?: false
        },
        isAutoCreate = true
    ).apply {
        refreshContainerView?.addView(smartRefreshLayout)
    }
}

