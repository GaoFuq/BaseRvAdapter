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


inline fun <reified T : ViewDataBinding> BaseVH.get(): T {
    return this.vhBinding as T
}


inline fun <reified T : RVSelect> FragmentActivity.refreshHelperAutoCreate(
    itemLayoutId: Int,
    containerView: ViewGroup,
    stateView: IStateView? = null,
    dataPerPage: Int = 10,
    crossinline bindAdapterItemView: (adapter: BaseRVAdapter<T>, holder: BaseVH, data: T, position: Int) -> Unit,
    noinline queryRAMCachedData: ((RefreshHelper<T>) -> List<T>?)? = null,
    noinline queryDBCachedData: ((RefreshHelper<T>) -> List<T>?)? = null,
    noinline request: (curPage: Int, dataPerPage: Int, callback: (List<T>?) -> Unit) -> Unit,
    noinline onStateChange: ((helper: RefreshHelper<T>, state: State) -> Boolean)? = null,
): RefreshHelper<T> {
    return RefreshHelper(
        autoCreate = true,
        activityOrFragment = this,
        adapter = object : BaseRVAdapter<T>(itemLayoutId) {
            override fun onBindView(holder: BaseVH, data: T, position: Int) {
                bindAdapterItemView(this, holder, data, position)
            }
        },
        queryRAMCachedData = queryRAMCachedData,
        queryDBCachedData = queryDBCachedData,
        requestData = request,
        stateView = stateView,
        dataPerPage = dataPerPage,
        onStateChange = onStateChange
    ).apply {
        containerView.addView(smartRefreshLayout, -1, -1)
    }
}


inline fun <reified T : RVSelect> Fragment.refreshHelperAutoCreate(
    itemLayoutId: Int,
    containerView: ViewGroup,
    stateView: IStateView? = null,
    dataPerPage: Int = 10,
    crossinline bindAdapterItemView: (adapter: BaseRVAdapter<T>, holder: BaseVH, data: T, position: Int) -> Unit,
    noinline queryRAMCachedData: ((RefreshHelper<T>) -> List<T>?)? = null,
    noinline queryDBCachedData: ((RefreshHelper<T>) -> List<T>?)? = null,
    noinline request: (curPage: Int, dataPerPage: Int, callback: (List<T>?) -> Unit) -> Unit,
    noinline onStateChange: ((helper: RefreshHelper<T>, state: State) -> Boolean)? = null,
): RefreshHelper<T> {
    return RefreshHelper(
        autoCreate = true,
        activityOrFragment = this,
        adapter = object : BaseRVAdapter<T>(itemLayoutId) {
            override fun onBindView(holder: BaseVH, data: T, position: Int) {
                bindAdapterItemView(this, holder, data, position)
            }
        },
        queryRAMCachedData = queryRAMCachedData,
        queryDBCachedData = queryDBCachedData,
        requestData = request,
        stateView = stateView,
        dataPerPage = dataPerPage,
        onStateChange = onStateChange
    ).apply {
        containerView.addView(smartRefreshLayout, -1, -1)
    }
}


inline fun <reified T : RVSelect> FragmentActivity.refreshHelperNormalCreate(
    itemLayoutId: Int,
    smartRefreshLayout: SmartRefreshLayout,
    recyclerView: RecyclerView,
    stateView: IStateView? = null,
    dataPerPage: Int = 10,
    crossinline bindAdapterItemView: (adapter: BaseRVAdapter<T>, holder: BaseVH, data: T, position: Int) -> Unit,
    noinline queryRAMCachedData: ((RefreshHelper<T>) -> List<T>?)? = null,
    noinline queryDBCachedData: ((RefreshHelper<T>) -> List<T>?)? = null,
    noinline request: (curPage: Int, dataPerPage: Int, callback: (List<T>?) -> Unit) -> Unit,
    noinline onStateChange: ((helper: RefreshHelper<T>, state: State) -> Boolean)? = null,
): RefreshHelper<T> {
    return RefreshHelper(
        autoCreate = false,
        activityOrFragment = this,
        adapter = object : BaseRVAdapter<T>(itemLayoutId) {
            override fun onBindView(holder: BaseVH, data: T, position: Int) {
                bindAdapterItemView(this, holder, data, position)
            }
        },
        queryRAMCachedData = queryRAMCachedData,
        queryDBCachedData = queryDBCachedData,
        requestData = request,
        stateView = stateView,
        smartRefreshLayout = smartRefreshLayout,
        recyclerView = recyclerView,
        dataPerPage = dataPerPage,
        onStateChange = onStateChange
    )
}


inline fun <reified T : RVSelect> Fragment.refreshHelperNormalCreate(
    itemLayoutId: Int,
    smartRefreshLayout: SmartRefreshLayout,
    recyclerView: RecyclerView,
    stateView: IStateView? = null,
    dataPerPage: Int = 10,
    crossinline bindAdapterItemView: (adapter: BaseRVAdapter<T>, holder: BaseVH, data: T, position: Int) -> Unit,
    noinline queryRAMCachedData: ((RefreshHelper<T>) -> List<T>?)? = null,
    noinline queryDBCachedData: ((RefreshHelper<T>) -> List<T>?)? = null,
    noinline request: (curPage: Int, dataPerPage: Int, callback: (List<T>?) -> Unit) -> Unit,
    noinline onStateChange: ((helper: RefreshHelper<T>, state: State) -> Boolean)? = null,
): RefreshHelper<T> {
    return RefreshHelper(
        autoCreate = false,
        activityOrFragment = this,
        adapter = object : BaseRVAdapter<T>(itemLayoutId) {
            override fun onBindView(holder: BaseVH, data: T, position: Int) {
                bindAdapterItemView(this, holder, data, position)
            }
        },
        queryRAMCachedData = queryRAMCachedData,
        queryDBCachedData = queryDBCachedData,
        requestData = request,
        smartRefreshLayout = smartRefreshLayout,
        recyclerView = recyclerView,
        stateView = stateView,
        dataPerPage = dataPerPage,
        onStateChange = onStateChange
    )
}
