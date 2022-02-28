package com.gfq.baservadapter.refresh


import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gfq.baservadapter.R
import com.gfq.baservadapter.adapter.BaseRVAdapter
import com.gfq.baservadapter.databinding.RefreshHelperLayoutBinding
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.MaterialHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.constant.SpinnerStyle
import androidx.fragment.app.FragmentActivity

/**
 * * 自动创建：[FragmentActivity.refreshHelperAutoCreate]。
 * * 自动创建：[Fragment.refreshHelperAutoCreate]。
 * * 手动创建：[FragmentActivity.refreshHelperNormalCreate]。
 * * 手动创建：[Fragment.refreshHelperNormalCreate]。
 * *
 * * 自动创建的 RefreshHelper 才有 [topViewContainer],[bottomViewContainer]。
 * * 自动创建的 RefreshHelper [stateViewLoadMoreNoMoreData] 才会生效。
 */
class RefreshHelper<DataBean>(
    val activityOrFragment: Any,
    val adapter: BaseRVAdapter<DataBean>,
    var smartRefreshLayout: SmartRefreshLayout? = null,
    var recyclerView: RecyclerView? = null,
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

    //recyclerView 的容器；无网络，无数据等视图显示的区域
    lateinit var stateViewContainer: FrameLayout
        private set

    //在 recyclerView 上面的 view 容器
    var topViewContainer: FrameLayout? = null
        private set

    //在 recyclerView 下面的 view 容器
    var bottomViewContainer: FrameLayout? = null
        private set

    var isAutoRefreshOnCreate: Boolean = true
        set(value) {
            field = value
            if(value){
                isAutoRefreshOnResume = false
            }
        }

    var isAutoRefreshOnResume: Boolean = false
        set(value) {
            field = value
            if(value){
                isAutoRefreshOnCreate = false
            }
        }

    var isEnableRefresh: Boolean = true
        set(value) {
            field = value
            smartRefreshLayout?.setEnableRefresh(value)
        }

    var isEnableLoadMore: Boolean = true
        set(value) {
            field = value
            smartRefreshLayout?.setEnableLoadMore(value)
        }

    private var state: State = State.LOADING

    //当前覆盖在 recyclerView 之上的view
    private var coverView: View? = null

    //覆盖 recyclerView 的状态view
    var stateViewLoading: View? = null
    var stateViewEmptyData: View? = null
    var stateViewNetLose: View? = null
    var stateViewError: View? = null

    //加载更多，没有更多数据时，显示在最下方的view
    var stateViewLoadMoreNoMoreData: View? = null
        set(value) {
            field = value
            bottomViewContainer?.addView(value)
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

        autoCreateIfNeed()


        if (adapter.recyclerView == null) {
            adapter.recyclerView = recyclerView
        }

        if (recyclerView?.layoutManager == null) {
            recyclerView?.layoutManager = LinearLayoutManager(context)
        }


        smartRefreshLayout?.run {
            setEnableLoadMore(isEnableLoadMore)
            setEnableRefresh(isEnableRefresh)
            setRefreshHeader(MaterialHeader(context))
            setRefreshFooter(ClassicsFooter(context).setSpinnerStyle(SpinnerStyle.FixedBehind))
            setOnRefreshListener {
                callRefresh(it)
            }

            setOnLoadMoreListener {
                callLoadMore(it)
            }
        }



        initStateView()


    }

    private fun autoCreateIfNeed() {
        if (smartRefreshLayout == null) {
            Log.d(tag,"auto create")
            val binding =
                DataBindingUtil.inflate<RefreshHelperLayoutBinding>(LayoutInflater.from(context),
                    R.layout.refresh_helper_layout,
                    null,
                    false)
            smartRefreshLayout = binding.smartRefreshLayout
            recyclerView = binding.recyclerView
            stateViewContainer = binding.stateViewContainer
            topViewContainer = binding.topViewContainer
            bottomViewContainer = binding.bottomViewContainer
        } else {
            Log.d(tag,"user xml create")
            val parent = this.recyclerView?.parent
            if (parent != null && parent is FrameLayout) {
                stateViewContainer = parent
            } else {
                throw RuntimeException("recyclerView  的 parentView 必须是 FrameLayout")
            }
        }
    }


    private fun initStateView() {
        stateViewLoading = stateView?.loadingView(context, this)
        stateViewEmptyData = stateView?.emptyDataView(context, this)
        stateViewLoadMoreNoMoreData = stateView?.emptyDataWithLoadMoreView(context, this)
        stateViewNetLose = stateView?.netLoseView(context, this)
    }


    private fun callLoadMore(refreshLayout: RefreshLayout) {
        if (isNetworkConnected(context)) {
            doLoadMore(refreshLayout)
        } else {
            refreshLayout.finishLoadMore(false)
            updateRefreshState(State.NET_LOSE)
        }
    }

    private fun callRefresh(refreshLayout: RefreshLayout) {
        if (isNetworkConnected(context)) {
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
                        refreshLayout.finishLoadMore(true)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            updateRefreshState(State.ERROR)
            refreshLayout.finishLoadMore(false)
        }
    }


    private fun doRefresh(refreshLayout: RefreshLayout) {
        currentPage = 1
        if (requestData == null) return
        updateRefreshState(State.LOADING)
        try {
            requestData.invoke(currentPage, dataPerPage) {
                when {
                    it.isNullOrEmpty() -> {
                        if (adapter.dataList.isEmpty()) {
                            updateRefreshState(State.EMPTY_DATA)
                        }
                        refreshLayout.finishRefreshWithNoMoreData()
                    }
                    else -> {
                        adapter.dataList = it as MutableList<DataBean>
                        refreshLayout.finishRefresh(true)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            updateRefreshState(State.ERROR)
            refreshLayout.finishRefresh(false)
        }
    }


    private fun updateRefreshState(state: State) {
        if (this.state == state) return
        this.state = state
        Log.d(tag, "onStateChange state = " + state.name)
        val intercept = onStateChange?.invoke(this, state)
        if (intercept == null || intercept == false) {
            when (state) {
                State.LOADING -> stateViewLoading?.let {
                    coverView = it
                    stateViewContainer.addView(it)
                    stateViewLoadMoreNoMoreData?.isGone = true
                }
                State.EMPTY_DATA -> stateViewEmptyData?.let {
                    coverView = it
                    stateViewContainer.addView(it)
                    stateViewLoadMoreNoMoreData?.isGone = true
                }

                State.EMPTY_DATA_WITH_LOAD_MORE -> stateViewLoadMoreNoMoreData?.isVisible = true

                State.NET_LOSE -> stateViewNetLose?.let {
                    coverView = it
                    stateViewContainer.addView(it)
                    stateViewLoadMoreNoMoreData?.isGone = true
                }

                State.ERROR -> stateViewError?.let {
                    coverView = it
                    stateViewContainer.addView(it)
                    stateViewLoadMoreNoMoreData?.isGone = true
                }

                State.SHOW_CONTENT -> {
                    coverView?.let {
                        stateViewContainer.removeView(it)
                    }
                    stateViewLoadMoreNoMoreData?.isGone = true
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
            smartRefreshLayout?.let { callRefresh(it) }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun autoRefreshOnCreate() {
        if (isAutoRefreshOnCreate && isEnableRefresh) {
            Log.d(tag, "autoRefreshOnCreate")
            smartRefreshLayout?.let { callRefresh(it) }
        }
    }


}