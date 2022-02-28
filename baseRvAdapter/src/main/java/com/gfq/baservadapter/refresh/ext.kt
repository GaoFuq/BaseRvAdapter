package com.gfq.baservadapter.refresh

import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.gfq.baservadapter.adapter.BaseRVAdapter
import com.gfq.baservadapter.adapter.BaseVH
import com.scwang.smart.refresh.layout.SmartRefreshLayout


/**
 *  2021/6/2 14:41
 * @auth gaofuq
 * @description
 */
/**
 * 继承 RVSelectBean 使其具有 select , type 属性
 * 继承 RVTypeBean 使其具有  viewType 属性
 */
open class RVSelectBean(open var select: Boolean = false) : RVTypeBean()

open class RVTypeBean(open val viewType: Int? = null)

inline fun <reified T : ViewDataBinding> BaseVH.get(): T {
    return this.vhBinding as T
}


inline fun <reified T : RVTypeBean> FragmentActivity.refreshHelperAutoCreate(
    itemLayoutId: Int,
    containerView: ViewGroup,
    stateView: IStateView? = null,
    dataPerPage: Int = 10,
    crossinline bindAdapterItemView: (adapter: BaseRVAdapter<T>, holder: BaseVH, data: T, position: Int) -> Unit,
    noinline request: (curPage: Int, pageDataCount: Int, callback: (List<T>?) -> Unit) -> Unit,
    noinline onStateChange: ((helper: RefreshHelper<T>, state: State) -> Boolean)? = null,
): RefreshHelper<T> {
    return RefreshHelper(
        activityOrFragment = this,
        adapter = object : BaseRVAdapter<T>(itemLayoutId) {
            override fun onBindView(holder: BaseVH, data: T, position: Int) {
                bindAdapterItemView(this, holder, data, position)
            }
        },
        requestData = request,
        stateView = stateView,
        dataPerPage = dataPerPage,
        onStateChange = onStateChange
    ).apply {
        containerView.addView(smartRefreshLayout)
    }
}


inline fun <reified T : RVTypeBean> Fragment.refreshHelperAutoCreate(
    itemLayoutId: Int,
    containerView: ViewGroup,
    stateView: IStateView? = null,
    dataPerPage: Int = 10,
    crossinline bindAdapterItemView: (adapter: BaseRVAdapter<T>, holder: BaseVH, data: T, position: Int) -> Unit,
    noinline request: (curPage: Int, pageDataCount: Int, callback: (List<T>?) -> Unit) -> Unit,
    noinline onStateChange: ((helper: RefreshHelper<T>, state: State) -> Boolean)? = null,
): RefreshHelper<T> {
    return RefreshHelper(
        activityOrFragment = this,
        adapter = object : BaseRVAdapter<T>(itemLayoutId) {
            override fun onBindView(holder: BaseVH, data: T, position: Int) {
                bindAdapterItemView(this, holder, data, position)
            }
        },
        requestData = request,
        stateView = stateView,
        dataPerPage = dataPerPage,
        onStateChange = onStateChange
    ).apply {
        containerView.addView(smartRefreshLayout)
    }
}


inline fun <reified T : RVTypeBean> FragmentActivity.refreshHelperNormalCreate(
    itemLayoutId: Int,
    smartRefreshLayout: SmartRefreshLayout,
    recyclerView: RecyclerView,
    stateView: IStateView? = null,
    dataPerPage: Int = 10,
    crossinline bindAdapterItemView: (adapter: BaseRVAdapter<T>, holder: BaseVH, data: T, position: Int) -> Unit,
    noinline request: (curPage: Int, pageDataCount: Int, callback: (List<T>?) -> Unit) -> Unit,
    noinline onStateChange: ((helper: RefreshHelper<T>, state: State) -> Boolean)? = null,
): RefreshHelper<T> {
    return RefreshHelper(
        activityOrFragment = this,
        smartRefreshLayout=smartRefreshLayout,
        recyclerView=recyclerView,
        adapter = object : BaseRVAdapter<T>(itemLayoutId) {
            override fun onBindView(holder: BaseVH, data: T, position: Int) {
                bindAdapterItemView(this, holder, data, position)
            }
        },
        requestData = request,
        stateView = stateView,
        dataPerPage = dataPerPage,
        onStateChange = onStateChange
    )
}


inline fun <reified T : RVTypeBean> Fragment.refreshHelperNormalCreate(
    itemLayoutId: Int,
    smartRefreshLayout: SmartRefreshLayout,
    recyclerView: RecyclerView,
    stateView: IStateView? = null,
    dataPerPage: Int = 10,
    crossinline bindAdapterItemView: (adapter: BaseRVAdapter<T>, holder: BaseVH, data: T, position: Int) -> Unit,
    noinline request: (curPage: Int, pageDataCount: Int, callback: (List<T>?) -> Unit) -> Unit,
    noinline onStateChange: ((helper: RefreshHelper<T>, state: State) -> Boolean)? = null,
): RefreshHelper<T> {
    return RefreshHelper(
        activityOrFragment = this,
        smartRefreshLayout=smartRefreshLayout,
        recyclerView=recyclerView,
        adapter = object : BaseRVAdapter<T>(itemLayoutId) {
            override fun onBindView(holder: BaseVH, data: T, position: Int) {
                bindAdapterItemView(this, holder, data, position)
            }
        },
        requestData = request,
        stateView = stateView,
        dataPerPage = dataPerPage,
        onStateChange = onStateChange
    )
}
