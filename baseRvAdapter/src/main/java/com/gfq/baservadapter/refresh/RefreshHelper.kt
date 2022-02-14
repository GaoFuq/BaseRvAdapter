package com.gfq.baservadapter.refresh


import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import androidx.activity.ComponentActivity
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gfq.baservadapter.BaseRVAdapter
import com.gfq.baservadapter.R
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
    val activityOrFragment: Any,
    val smartRefreshLayout: SmartRefreshLayout,
    val recyclerView: RecyclerView,
    val adapter: BaseRVAdapter<DataBean>,
    /**
     * 当前页索引
     */
    var currentPage: Int = 1,
    /**
     * 每页数据量
     */
    private val dataPerPage: Int = 10,
    /**
     * 总页数
     */
    private val totalPage: Int = 999,
    val isAutoRefreshOnCreate: Boolean = true,
    val isAutoRefreshOnResume: Boolean = false,
    val isEnableRefresh: Boolean = true,
    val isEnableLoadMore: Boolean = true,
    private val isAutoCreate: Boolean = false,
    private val stateView: IStateView? = null,
    private val requestData: ((curPage: Int, pageDataCount: Int, callback: (List<DataBean>?) -> Unit) -> Unit)? = null,
    /**
     * @return 是否拦截状态视图的显示处理。默认会显示[stateView]提供的View，并且该View会覆盖recyclerView。
     * @see [updateRefreshState]
     */
    private val onStateChange: ((helper: RefreshHelper<DataBean>, state: State) -> Boolean)? = null,
) : LifecycleObserver {
    private val tag = "【RefreshHelper】"
    lateinit var context: Context
        private set

    //无网络，无数据等视图显示的区域
    lateinit var stateViewContainer: RelativeLayout
        private set

    private var state: State = State.IDLE

    var stateViewRefreshLoading: View? = null
    var stateViewRefreshSuccess: View? = null
    var stateViewRefreshError: View? = null

    var stateViewLoadMoreLoading: View? = null
    var stateViewLoadMoreSuccess: View? = null
    var stateViewLoadMoreError: View? = null

    var stateViewEmptyData: View? = null
    var stateViewEmptyDataWithRefresh: View? = null
    var stateViewEmptyDataWithLoadMore: View? = null

    var stateViewNetLose: View? = null


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


    init {
        if (activityOrFragment is ComponentActivity) {
            activityOrFragment.lifecycle.addObserver(this)
            context = activityOrFragment
            Log.d(tag, "init context is ${activityOrFragment.componentName.className}")
        } else if (activityOrFragment is Fragment) {
            activityOrFragment.parentFragment?.lifecycle?.addObserver(this)
            activityOrFragment.context?.let { context = it }
            Log.d(tag, "init context is Fragment , tag = ${activityOrFragment.tag}")
        }

        checkStateViewContainer()

        recyclerView.visibility = View.VISIBLE

        if (adapter.recyclerView == null) {
            adapter.recyclerView = recyclerView
        }

        if (recyclerView.layoutManager == null) {
            recyclerView.layoutManager = LinearLayoutManager(context)
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
            recyclerView.id = R.id.refresh_recycler_view
            smartRefreshLayout.addView(stateViewContainer, -1, -1)
            stateViewContainer.addView(recyclerView, -1, -1)
        }

        initStateView()


    }

    private fun initStateView() {
        stateViewRefreshLoading = stateView?.refreshLoadingView(context, this)
        stateViewRefreshSuccess = stateView?.refreshSuccessView(context, this)
        stateViewRefreshError = stateView?.refreshErrorView(context, this)

        stateViewLoadMoreLoading = stateView?.loadMoreLoadingView(context, this)
        stateViewLoadMoreSuccess = stateView?.loadMoreSuccessView(context, this)
        stateViewLoadMoreError = stateView?.loadMoreErrorView(context, this)

        stateViewEmptyData = stateView?.emptyDataView(context, this)
        stateViewEmptyDataWithRefresh = stateView?.emptyDataWithRefreshView(context, this)
        stateViewEmptyDataWithLoadMore = stateView?.emptyDataWithLoadMoreView(context, this)

        stateViewNetLose = stateView?.netLoseView(context, this)
    }


    private fun callLoadMore(refreshLayout: RefreshLayout) {
        if (isNetworkConnected(context)) {
            updateRefreshState(State.IDLE)
            doLoadMore(refreshLayout)
        } else {
            refreshLayout.finishLoadMore(false)
            updateRefreshState(State.NET_LOSE)
        }
    }

    private fun callRefresh(refreshLayout: RefreshLayout) {
        if (isNetworkConnected(context)) {
            updateRefreshState(State.IDLE)
            doRefresh(refreshLayout)
        } else {
            refreshLayout.finishRefresh(false)
            updateRefreshState(State.NET_LOSE)
        }
    }

    private fun doLoadMore(refreshLayout: RefreshLayout) {
        currentPage++
        if (currentPage > totalPage) {
            currentPage = totalPage
            refreshLayout.finishLoadMoreWithNoMoreData()
            return
        }
        if (requestData == null) return
        updateRefreshState(State.LOAD_MORE_LOADING)
        try {
            requestData.invoke(currentPage, dataPerPage) {
                when {
                    it.isNullOrEmpty() -> {
                        currentPage--
                        if (adapter.dataList.isEmpty()) {
                            updateRefreshState(State.EMPTY_DATA)
                        } else {
                            updateRefreshState(State.EMPTY_DATA_WITH_LOAD_MORE)
                        }
                        refreshLayout.finishLoadMoreWithNoMoreData()
                    }
                    else -> {
                        adapter.addAll(it)
                        updateRefreshState(State.LOAD_MORE_SUCCESS)
                        refreshLayout.finishLoadMore(true)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            updateRefreshState(State.LOAD_MORE_ERROR)
            refreshLayout.finishLoadMore(false)
        }
    }


    private fun doRefresh(refreshLayout: RefreshLayout) {
        currentPage = 1
        if (requestData == null) return
        updateRefreshState(State.REFRESH_LOADING)
        try {
            requestData.invoke(currentPage, dataPerPage) {
                when {
                    it.isNullOrEmpty() -> {
                        if (adapter.dataList.isEmpty()) {
                            updateRefreshState(State.EMPTY_DATA)
                        } else {
                            updateRefreshState(State.EMPTY_DATA_WITH_REFRESH)
                        }
                        refreshLayout.finishRefreshWithNoMoreData()
                    }
                    else -> {
                        updateRefreshState(State.REFRESH_SUCCESS)
                        adapter.dataList = it as MutableList<DataBean>
                        refreshLayout.finishRefresh(true)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            updateRefreshState(State.REFRESH_ERROR)
            refreshLayout.finishRefresh(false)
        }
    }


    private fun updateRefreshState(state: State) {
        if (this.state == state) return
        this.state = state
        Log.d(tag,"onStateChange state = "+state.name)
        val intercept = onStateChange?.invoke(this, state)
        if (intercept == null || intercept == false) {
            stateViewContainer.children.forEach {
                if(it.id != R.id.refresh_recycler_view){
                    stateViewContainer.removeView(it)
                }
            }
            when (state) {
                State.IDLE ->{}
                State.REFRESH_LOADING -> stateViewRefreshLoading?.let {
                    stateViewContainer.addView(it,-1,-1)
                }
                State.REFRESH_SUCCESS -> stateViewRefreshSuccess?.let {
                    stateViewContainer.addView(it,-1,-1)
                }
                State.REFRESH_ERROR -> stateViewRefreshError?.let {
                    stateViewContainer.addView(it,-1,-1)
                }
                State.LOAD_MORE_LOADING -> stateViewLoadMoreLoading?.let {
                    stateViewContainer.addView(it,-1,-1)
                }
                State.LOAD_MORE_SUCCESS -> stateViewLoadMoreSuccess?.let {
                    stateViewContainer.addView(it,-1,-1)
                }
                State.LOAD_MORE_ERROR -> stateViewLoadMoreError?.let {
                    stateViewContainer.addView(it,-1,-1)
                }
                State.EMPTY_DATA -> stateViewEmptyData?.let {
                    stateViewContainer.addView(it,-1,-1)
                }
                State.EMPTY_DATA_WITH_LOAD_MORE -> stateViewEmptyDataWithLoadMore?.let {
                    stateViewContainer.addView(it,-1,-1)
                }
                State.EMPTY_DATA_WITH_REFRESH -> stateViewEmptyDataWithRefresh?.let {
                        stateViewContainer.addView(it,-1,-1)
                    }
                State.NET_LOSE -> stateViewNetLose?.let {
                    stateViewContainer.addView(it,-1,-1)
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
            Log.d(tag, "autoRefreshOnResume")
            callRefresh(smartRefreshLayout)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun autoRefreshOnCreate() {
        if (isAutoRefreshOnCreate && isEnableRefresh) {
            Log.d(tag, "autoRefreshOnCreate")
            callRefresh(smartRefreshLayout)
        }
    }


}