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

inline fun <reified T : ViewDataBinding> BaseVH.get(): T {
    return this.vhBinding as T
}


inline fun <reified T> FragmentActivity.refreshHelperAutoCreate(
    itemLayoutId: Int,
    containerView: ViewGroup,
    stateView: IStateView? = null,
    dataPerPage: Int,
    crossinline bindAdapterItemView: (adapter: BaseRVAdapter<T>, holder: BaseVH, data: T, position: Int) -> Unit,
    noinline request: (curPage: Int, dataPerPage: Int, callback: (List<T>?) -> Unit) -> Unit,
): RefreshHelper<T> {
    return RefreshHelper(
        autoCreate = true,
        activityOrFragment = this,
        adapter = object : BaseRVAdapter<T>(itemLayoutId) {
            override fun onBindView(holder: BaseVH, data: T, position: Int) {
                bindAdapterItemView(this, holder, data, position)
            }
        },
        requestData = request,
        stateView = stateView,
        dataPerPage = dataPerPage,
    ).apply {
        containerView.addView(smartRefreshLayout, -1, -1)
    }
}

inline fun <reified T> FragmentActivity.refreshHelperAutoCreate(
    containerView: ViewGroup,
    stateView: IStateView? = null,
    dataPerPage: Int,
    adapter: BaseRVAdapter<T>,
    noinline request: (curPage: Int, dataPerPage: Int, callback: (List<T>?) -> Unit) -> Unit,
): RefreshHelper<T> {
    return RefreshHelper(
        autoCreate = true,
        activityOrFragment = this,
        adapter = adapter,
        requestData = request,
        stateView = stateView,
        dataPerPage = dataPerPage,
    ).apply {
        containerView.addView(smartRefreshLayout, -1, -1)
    }
}



inline fun <reified T> Fragment.refreshHelperAutoCreate(
    containerView: ViewGroup,
    stateView: IStateView? = null,
    dataPerPage: Int,
    adapter:BaseRVAdapter<T>,
    noinline request: (curPage: Int, dataPerPage: Int, callback: (List<T>?) -> Unit) -> Unit,
): RefreshHelper<T> {
    return RefreshHelper(
        autoCreate = true,
        activityOrFragment = this,
        adapter = adapter,
        requestData = request,
        stateView = stateView,
        dataPerPage = dataPerPage,
    ).apply {
        containerView.addView(smartRefreshLayout, -1, -1)
    }
}


inline fun <reified T> FragmentActivity.refreshHelperNormalCreate(
    itemLayoutId: Int,
    smartRefreshLayout: SmartRefreshLayout,
    recyclerView: RecyclerView,
    stateView: IStateView? = null,
    dataPerPage: Int,
    crossinline bindAdapterItemView: (adapter: BaseRVAdapter<T>, holder: BaseVH, data: T, position: Int) -> Unit,
    noinline request: (curPage: Int, dataPerPage: Int, callback: (List<T>?) -> Unit) -> Unit,
): RefreshHelper<T> {
    return RefreshHelper(
        autoCreate = false,
        activityOrFragment = this,
        adapter = object : BaseRVAdapter<T>(itemLayoutId) {
            override fun onBindView(holder: BaseVH, data: T, position: Int) {
                bindAdapterItemView(this, holder, data, position)
            }
        },
        requestData = request,
        stateView = stateView,
        smartRefreshLayout = smartRefreshLayout,
        recyclerView = recyclerView,
        dataPerPage = dataPerPage,
    )
}


inline fun <reified T> FragmentActivity.refreshHelperNormalCreate(
    smartRefreshLayout: SmartRefreshLayout,
    recyclerView: RecyclerView,
    stateView: IStateView? = null,
    dataPerPage: Int,
    adapter: BaseRVAdapter<T>,
    noinline request: (curPage: Int, dataPerPage: Int, callback: (List<T>?) -> Unit) -> Unit,
): RefreshHelper<T> {
    return RefreshHelper(
        autoCreate = false,
        activityOrFragment = this,
        adapter =adapter,
        requestData = request,
        stateView = stateView,
        smartRefreshLayout = smartRefreshLayout,
        recyclerView = recyclerView,
        dataPerPage = dataPerPage,
    )
}


inline fun <reified T> Fragment.refreshHelperNormalCreate(
    itemLayoutId: Int,
    smartRefreshLayout: SmartRefreshLayout,
    recyclerView: RecyclerView,
    stateView: IStateView? = null,
    dataPerPage: Int,
    crossinline bindAdapterItemView: (adapter: BaseRVAdapter<T>, holder: BaseVH, data: T, position: Int) -> Unit,
    noinline request: (curPage: Int, dataPerPage: Int, callback: (List<T>?) -> Unit) -> Unit,
): RefreshHelper<T> {
    return RefreshHelper(
        autoCreate = false,
        activityOrFragment = this,
        adapter = object : BaseRVAdapter<T>(itemLayoutId) {
            override fun onBindView(holder: BaseVH, data: T, position: Int) {
                bindAdapterItemView(this, holder, data, position)
            }
        },
        requestData = request,
        smartRefreshLayout = smartRefreshLayout,
        recyclerView = recyclerView,
        stateView = stateView,
        dataPerPage = dataPerPage,
    )
}


inline fun <reified T> Fragment.refreshHelperNormalCreate(
    smartRefreshLayout: SmartRefreshLayout,
    recyclerView: RecyclerView,
    stateView: IStateView? = null,
    dataPerPage: Int,
    adapter: BaseRVAdapter<T>,
    noinline request: (curPage: Int, dataPerPage: Int, callback: (List<T>?) -> Unit) -> Unit,
): RefreshHelper<T> {
    return RefreshHelper(
        autoCreate = false,
        activityOrFragment = this,
        adapter = adapter,
        requestData = request,
        smartRefreshLayout = smartRefreshLayout,
        recyclerView = recyclerView,
        stateView = stateView,
        dataPerPage = dataPerPage,
    )
}
