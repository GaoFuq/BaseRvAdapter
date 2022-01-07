package com.gfq.baservadapter


import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import androidx.activity.ComponentActivity
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.RecyclerView
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshLayout

/**
 *  2021/5/10 11:07
 * @auth gaofuq
 * @description
 *
<com.scwang.smart.refresh.layout.SmartRefreshLayout
android:id="@+id/smartRefreshLayout"
android:layout_width="match_parent"
android:layout_height="match_parent">

<RelativeLayout
android:layout_width="match_parent"
android:layout_height="match_parent">

<androidx.recyclerview.widget.RecyclerView
android:id="@+id/recyclerView"
app:layoutManager="androidx.recyclerview.widget.LinearLayoutManager"
android:layout_width="match_parent"
android:layout_height="match_parent"/>

</RelativeLayout>
</com.scwang.smart.refresh.layout.SmartRefreshLayout>


1.自己赋值
RefreshHelper(
context = this,
smartRefreshLayout = binding.smartRefreshLayout,
recyclerView = binding.recyclerView,
adapter = adapter,
requestData = { curPage, pageDataNumber, callback ->
callback(getDataList(curPage, pageDataNumber))
},
onRefreshStateChange = {
Log.e("xxxx", "onRefreshStateChange " + it.name)
false
}
)

2.代码自动创建
createRefreshHelper<String>(
context = this,
itemLayoutId = R.layout.select_item,
refreshContainerView = null,
onAdapterBindView ={holder: BaseVH, data: String, position: Int ->

},
request = {curPage: Int, pageCount: Int, callback: (List<String>?) -> Unit ->

},
onRefreshStateChange = {
false
}
) .also {
//            it.smartRefreshLayout.
//            it.recyclerView.
//            it.adapter.
//            it.
}
 */
class RefreshHelper<DataBean>(
    val activityOrFragment: Any?,
    val smartRefreshLayout: SmartRefreshLayout,
    val recyclerView: RecyclerView,
    val adapter: BaseRVAdapter<DataBean>,
    var curPage: Int = 1,
    val pageDataCount: Int = 10,
    val pageTotalCount: Int = 999,
    val isAutoRefreshOnCreate: Boolean = true,
    val isAutoRefreshOnResume: Boolean = false,
    val isEnableRefresh: Boolean = true,
    val isEnableLoadMore: Boolean = true,
    val requestData: ((curPage: Int, pageDataCount: Int, callback: (List<DataBean>?) -> Unit) -> Unit)? = null,
    val onRefreshStateChange: ((state: RefreshState) -> Boolean)? = null,
    val isAutoCreate: Boolean = false
) : LifecycleObserver {
    private val tag = "【RefreshHelper】"
    var context: Context? = null
    var stateViewContainer: RelativeLayout? = null//无网络，无数据视图显示的区域
    var loadingView: View? = null
    var endSuccessView: View? = null
    var endFailedView: View? = null
    var emptyView: View? = null
    var loseNetView: View? = null
    var errorView: View? = null
    var refreshState: RefreshState = RefreshState.normal

    //recyclerView  的parentView 必须是 RelativeLayout
    private fun checkStateViewContainer() {
        if (isAutoCreate) {
            stateViewContainer = RelativeLayout(context)
        } else {
            val parent = this.recyclerView.parent
            if (parent != null && parent is RelativeLayout) {
                stateViewContainer = parent
            } else {
                throw RuntimeException("recyclerView  的 parentView 必须是 RelativeLayout")
            }
        }
    }

    private fun updateRefreshStateView(refreshState: RefreshState) {
        if (this.refreshState == refreshState) return
        this.refreshState = refreshState
        val intercept = onRefreshStateChange?.invoke(refreshState)
        if (intercept == false) {
            loadingView?.visibility = View.GONE
            endSuccessView?.visibility = View.GONE
            endFailedView?.visibility = View.GONE
            emptyView?.visibility = View.GONE
            loseNetView?.visibility = View.GONE
            errorView?.visibility = View.GONE
            recyclerView.visibility = View.GONE
            when (refreshState) {
                RefreshState.normal -> recyclerView.visibility = View.VISIBLE
                RefreshState.loading -> loadingView?.visibility = View.VISIBLE
                RefreshState.endSuccess -> endSuccessView?.visibility = View.VISIBLE
                RefreshState.endFailed -> endFailedView?.visibility = View.VISIBLE
                RefreshState.empty -> emptyView?.visibility = View.VISIBLE
                RefreshState.loseNet -> loseNetView?.visibility = View.VISIBLE
                RefreshState.error -> errorView?.visibility = View.VISIBLE
            }
        }
    }

    init {
        if (activityOrFragment is ComponentActivity) {
            activityOrFragment.lifecycle.addObserver(this)
            context = activityOrFragment
            Log.e(tag, "init context is ${activityOrFragment.componentName.className}")
        } else if (activityOrFragment is Fragment) {
            activityOrFragment.parentFragment?.lifecycle?.addObserver(this)
            context = activityOrFragment.context
            Log.e(tag, "init context is Fragment , tag = ${activityOrFragment.tag}")
        }

        checkStateViewContainer()


        val li = LayoutInflater.from(context)
        loadingView = li.inflate(RefreshState.loading.layoutId, stateViewContainer, false)
        endSuccessView = li.inflate(RefreshState.endSuccess.layoutId, stateViewContainer, false)
        endFailedView = li.inflate(RefreshState.endFailed.layoutId, stateViewContainer, false)
        emptyView = li.inflate(RefreshState.empty.layoutId, stateViewContainer, false)
        loseNetView = li.inflate(RefreshState.loseNet.layoutId, stateViewContainer, false)
        errorView = li.inflate(RefreshState.error.layoutId, stateViewContainer, false)

        stateViewContainer?.let {
            it.addView(loadingView)
            it.addView(endSuccessView)
            it.addView(endFailedView)
            it.addView(emptyView)
            it.addView(loseNetView)
            it.addView(errorView)
        }
        loadingView?.visibility = View.GONE
        endSuccessView?.visibility = View.GONE
        endFailedView?.visibility = View.GONE
        emptyView?.visibility = View.GONE
        loseNetView?.visibility = View.GONE
        errorView?.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE

        if (adapter.recyclerView == null) {
            adapter.recyclerView = recyclerView
        }
        smartRefreshLayout.setEnableLoadMore(isEnableLoadMore)
        smartRefreshLayout.setEnableRefresh(isEnableRefresh)


        smartRefreshLayout.run {
            setRefreshHeader(ClassicsHeader(context))
            setRefreshFooter(ClassicsFooter(context))
            setOnRefreshListener {
                callRefresh(it)
            }

            setOnLoadMoreListener {
                callLoadMore(it)
            }
        }

        if (isAutoCreate) {
            smartRefreshLayout.addView(stateViewContainer, -1, -1)
            stateViewContainer?.addView(recyclerView, -1, -1)
        }
    }


    private fun callLoadMore(refreshLayout: RefreshLayout) {
        if (isNetworkConnected(context)) {
            updateRefreshStateView(RefreshState.normal)
            doLoadMore(refreshLayout)
        } else {
            refreshLayout.finishLoadMore(false)
            updateRefreshStateView(RefreshState.loseNet)
            Log.e(tag, "finishLoadMore failed loseNet")
        }
    }

    private fun callRefresh(refreshLayout: RefreshLayout) {
        Log.e(tag, "callRefresh")
        if (isNetworkConnected(context)) {
            updateRefreshStateView(RefreshState.normal)
            doRefresh(refreshLayout)
        } else {
            refreshLayout.finishRefresh(false)
            updateRefreshStateView(RefreshState.loseNet)
            Log.e(tag, "finishRefresh failed loseNet")
        }
    }

    private fun doLoadMore(refreshLayout: RefreshLayout) {
        Log.e(tag, "doLoadMore")
        curPage++
//        refreshLayout.autoLoadMore()
        if (curPage > pageTotalCount) {
            curPage = pageTotalCount
            refreshLayout.finishLoadMoreWithNoMoreData()
            Log.e(tag, "finishLoadMoreWithNoMoreData")
            return
        }
        if (requestData == null) return
        requestData.invoke(curPage, pageDataCount) {
            when {
                it == null -> {
                    curPage--
                    refreshLayout.finishLoadMore(false)
                    Log.e(tag, "finishLoadMore failed")
                }
                it.isEmpty() -> {
                    curPage--
                    refreshLayout.finishLoadMoreWithNoMoreData()
                    Log.e(tag, "finishLoadMoreWithNoMoreData")
                }
                else -> {
                    adapter.addAll(it)
                    refreshLayout.finishLoadMore(true)
                    Log.e(tag, "finishLoadMore success")
                }
            }
        }
    }


    private fun doRefresh(refreshLayout: RefreshLayout) {
        Log.e(tag, "doRefresh")
        curPage = 1
        if (requestData == null) return
        requestData.invoke(curPage, pageDataCount) {
            when {
                it == null -> {
                    updateRefreshStateView(RefreshState.error)
                    refreshLayout.finishRefresh(false)
                    Log.e(tag, "finishRefresh failed")
                }
                it.isEmpty() -> {
                    updateRefreshStateView(RefreshState.empty)
                    refreshLayout.finishRefreshWithNoMoreData()
                    Log.e(tag, "finishRefreshWithNoMoreData")
                }
                else -> {
                    updateRefreshStateView(RefreshState.normal)
                    adapter.dataList = it as MutableList<DataBean>
                    refreshLayout.finishRefresh(true)
                    Log.e(tag, "finishRefresh success")
                }
            }
        }

    }


    @SuppressLint("MissingPermission")
    private fun isNetworkConnected(context: Context?): Boolean {
        if (context != null) {
            val mConnectivityManager =
                context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            var mNetworkInfo: NetworkInfo? = null
            if (mConnectivityManager != null) {
                mNetworkInfo = mConnectivityManager.activeNetworkInfo
            }
            //获取连接对象
            if (mNetworkInfo != null) {
                //判断是TYPE_MOBILE网络
                if (ConnectivityManager.TYPE_MOBILE == mNetworkInfo.type) {
//                    LogManager.i("AppNetworkMgr", "网络连接类型为：TYPE_MOBILE");
                    //判断移动网络连接状态
                    val STATE_MOBILE =
                        mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)!!
                            .state
                    if (STATE_MOBILE == NetworkInfo.State.CONNECTED) {
//                        LogManager.i("AppNetworkMgrd", "网络连接类型为：TYPE_MOBILE, 网络连接状态CONNECTED成功！");
                        return mNetworkInfo.isAvailable
                    }
                }
                //判断是TYPE_WIFI网络
                if (ConnectivityManager.TYPE_WIFI == mNetworkInfo.type) {
//                    LogManager.i("AppNetworkMgr", "网络连接类型为：TYPE_WIFI");
                    //判断WIFI网络状态
                    val STATE_WIFI =
                        mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)!!
                            .state
                    if (STATE_WIFI == NetworkInfo.State.CONNECTED) {
//                        LogManager.i("AppNetworkMgr", "网络连接类型为：TYPE_WIFI, 网络连接状态CONNECTED成功！");
                        return mNetworkInfo.isAvailable
                    }
                }
            }
        }
        return false
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun autoRefreshOnResume() {
        if (isAutoRefreshOnResume && isEnableLoadMore) {
            Log.e(tag, "autoRefreshOnResume")
            callRefresh(smartRefreshLayout)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun autoRefreshOnCreate() {
        if (isAutoRefreshOnCreate && isEnableRefresh) {
            Log.e(tag, "autoRefreshOnCreate")
            callRefresh(smartRefreshLayout)
        }
    }


    enum class RefreshState(@LayoutRes val layoutId: Int) {
        normal(0),
        loading(R.layout.refresh_state_loading),
        endSuccess(R.layout.refresh_state_end_success),
        endFailed(R.layout.refresh_state_end_failed),
        empty(R.layout.refresh_state_empty),
        loseNet(R.layout.refresh_state_lose_net),
        error(R.layout.refresh_state_error)
    }
}