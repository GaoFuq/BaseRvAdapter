package com.gfq.baservadapter.refresh


import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import androidx.recyclerview.widget.*
import com.gfq.baservadapter.R
import com.gfq.baservadapter.adapter.BaseRVAdapter
import com.gfq.baservadapter.databinding.RefreshHelperLayoutBinding
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.MaterialHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.constant.SpinnerStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * *SmartRefreshLayout api文档 [https://gitee.com/scwang90/SmartRefreshLayout/raw/V-ART/art/md_property.md]
 *
 * * 自动创建布局：[R.layout.refresh_helper_layout]
 * * 自动创建：[FragmentActivity.refreshHelperAutoCreate]。
 * * 自动创建：[Fragment.refreshHelperAutoCreate]。
 * * 手动创建：[FragmentActivity.refreshHelperNormalCreate]。
 * * 手动创建：[Fragment.refreshHelperNormalCreate]。
 * *
 */
class RefreshHelper<DataBean>(
    val autoCreate: Boolean,
    val activityOrFragment: Any,
    val adapter: BaseRVAdapter<DataBean>,
    /**
     * 当前页索引
     */
    var currentPage: Int = 1,
    /**
     * 每页数据量
     */
    private val dataPerPage: Int,
    var smartRefreshLayout: SmartRefreshLayout? = null,
    var recyclerView: RecyclerView? = null,
    private val stateView: IStateView? = null,
    private val queryRAMCachedData: ((RefreshHelper<DataBean>) -> List<DataBean>?)? = null,
    private val queryDBCachedData: ((RefreshHelper<DataBean>) -> List<DataBean>?)? = null,
    private val requestData: ((curPage: Int, dataPerPage: Int, callback: (List<DataBean>?) -> Unit) -> Unit)? = null,
    /**
     * @return 是否拦截状态视图的显示处理。默认会显示[stateView]提供的View，并且该View会覆盖recyclerView。
     * @see [updateRefreshState]
     */
    private val onStateChange: ((helper: RefreshHelper<DataBean>, state: State) -> Unit)? = null,
) {

    private val tag = "【RefreshHelper】"

    /**
     * 总页数
     */
    var totalPage: Int = 999


    private var fetchFromCachedData = true
    private var isFirstCallRefresh = true


    //缓存的数据源
    private var cachedDataList: List<DataBean>? = null


    lateinit var context: Context
        private set

    //recyclerView 的容器；无网络，无数据等视图显示的区域
    lateinit var stateViewContainer: FrameLayout
        private set


    /**
     * 设置是否可以手动下拉刷新。
     */
    var isEnablePullDownRefresh: Boolean = true
        set(value) {
            field = value
            smartRefreshLayout?.setEnableRefresh(value)
        }

    /**
     * 设置是否可以手动上拉加载。
     */
    var isEnablePullUpLoad: Boolean = true
        set(value) {
            field = value
            smartRefreshLayout?.setEnableLoadMore(value)
        }

    /**
     * 设置是否开启预加载。
     */
    var isEnablePreLoadMore: Boolean = true
        set(value) {
            field = value
            if (field) {
                recyclerView?.addOnScrollListener(autoLoadMoreListener)
            } else {
                recyclerView?.removeOnScrollListener(autoLoadMoreListener)
            }

        }

    private var state: State = State.NONE

    //当前覆盖在 recyclerView 之上的view
    private var coverView: View? = null

    //覆盖 recyclerView 的状态view
    var stateViewLoading: View? = null
    var stateViewEmptyData: View? = null
    var stateViewNetLose: View? = null
    var stateViewError: View? = null


    private val autoLoadMoreListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                val layoutManager = recyclerView.layoutManager
                if (layoutManager is LinearLayoutManager) {
                    val lastItemPosition: Int =
                        layoutManager.findLastCompletelyVisibleItemPosition()
                    if (lastItemPosition == adapter.itemCount - 1
                        && smartRefreshLayout?.isLoading == false
                        && smartRefreshLayout?.isRefreshing == false
                    ) {
                        callLoadMore(false)
                    }
                }
            }

        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

        }
    }

    init {
        if (activityOrFragment is ComponentActivity) {
            context = activityOrFragment
            Log.d(tag, "init context is ${activityOrFragment.componentName.className}")
        } else if (activityOrFragment is Fragment) {
            activityOrFragment.context?.let { context = it }
            Log.d(tag,
                "init context is Fragment , name = ${activityOrFragment.javaClass.simpleName}")
        }

        if (!::context.isInitialized) {
            throw RuntimeException("context is not initialized")
        }

        autoCreateIfNeed()


//        setSupportChangeAnimation(false)

        if (recyclerView?.layoutManager == null) {
            recyclerView?.layoutManager = LinearLayoutManager(context)
        }

        recyclerView?.setHasFixedSize(true)

        isEnablePreLoadMore = false

        smartRefreshLayout?.run {
            setEnableLoadMore(isEnablePullUpLoad)
            setEnableRefresh(isEnablePullDownRefresh)
            setRefreshHeader(MaterialHeader(context))
            setEnableLoadMoreWhenContentNotFull(false)
            setEnableFooterFollowWhenNoMoreData(true)
            setRefreshFooter(ClassicsFooter(context).setSpinnerStyle(SpinnerStyle.FixedBehind))
            setOnRefreshListener {
                callRefresh(false)
            }

            setOnLoadMoreListener {
                callLoadMore(false)
            }
        }
        initStateView()
    }


    fun setSupportChangeAnimation(boolean: Boolean) {
        (recyclerView?.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = boolean
    }


    /**
     * 添加一个数据到列表中。默认添加到列表的最后面。
     */
    fun addData(data: DataBean?, position: Int = adapter.dataList.size) {
        if (data == null) {
            if (adapter.dataList.isEmpty()) {
                updateRefreshState(State.EMPTY_DATA)
            }
        } else {
            adapter.add(data, position)
            coverView?.let {
                stateViewContainer.removeView(it)
                coverView = null
            }
        }
    }


    /**
     * 设置数据源，会覆盖之前的所有数据
     */
    fun setData(list: List<DataBean>?) {
        if (list.isNullOrEmpty()) {
            if (adapter.dataList.isEmpty()) {
                updateRefreshState(State.EMPTY_DATA)
            }
        } else {
            adapter.dataList = list.toMutableList()
            coverView?.let {
                stateViewContainer.removeView(it)
                coverView = null
            }
        }
    }

    /**
     * 添加一个数据集合
     */
    fun addAll(list: List<DataBean>?, position: Int = adapter.dataList.size) {
        if (list.isNullOrEmpty()) {
            if (adapter.dataList.isEmpty()) {
                updateRefreshState(State.EMPTY_DATA)
            }
        } else {
            adapter.addAll(list, position)
            coverView?.let {
                stateViewContainer.removeView(it)
                coverView = null
            }
        }
    }


    private fun autoCreateIfNeed() {
        if (autoCreate) {
            Log.d(tag, "auto create")
            val binding =
                DataBindingUtil.inflate<RefreshHelperLayoutBinding>(LayoutInflater.from(context),
                    R.layout.refresh_helper_layout,
                    null,
                    false)
            smartRefreshLayout = binding.smartRefreshLayout
            recyclerView = binding.recyclerView
            stateViewContainer = binding.stateViewContainer
        } else {
            Log.d(tag, "user xml create")
            val tempView = smartRefreshLayout?.findViewById<FrameLayout>(R.id.stateViewContainer)
            if (tempView == null) {
                throw RuntimeException("必须设置一个 id = stateViewContainer 的 FrameLayout 作为状态容器")
            } else {
                stateViewContainer = tempView
            }
        }
    }


    private fun initStateView() {
        stateViewEmptyData = TextView(context).apply { text = "空页面" }
        stateViewLoading = stateView?.loadingView(context, this)

        stateViewNetLose = stateView?.netLoseView(context, this)

        stateView?.emptyDataView(context, this)?.let { stateViewEmptyData = it }
    }


    private fun doLoadMoreFromCachedData() {
        currentPage++
        if (currentPage > totalPage) {
            currentPage = totalPage
            updateRefreshState(State.NO_MORE_DATA_LOADMORE)
            smartRefreshLayout?.finishLoadMoreWithNoMoreData()
            return
        }

        try {
            val dataList = splitPage(currentPage, dataPerPage, cachedDataList)
            if (dataList.isNullOrEmpty()) {
                currentPage--
                fetchFromCachedData = false
                callLoadMore(true)
            } else {
                addAll(dataList)
                updateRefreshState(State.LOAD_MORE_SUCCESS)
                smartRefreshLayout?.finishLoadMore(true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            updateRefreshState(State.ERROR)
            smartRefreshLayout?.finishLoadMore(false)
        }
    }

    /**
     * 当列表的item数据发生改变后，调用该方法更新列表。避免网络刷新请求。
     * @param filter 判定item是否已经改变。
     */
    fun updateItemWhen(filter: (DataBean) -> Boolean, update: DataBean.() -> Unit) {
        adapter.updateItemWhen(filter, update)
    }

    /**
     * 当调用接口删除了列表的某些item后，调用该方法更新列表。避免网络刷新请求。
     * @param filter 判定item是否已被移除。
     */
    fun updateWhenItemDeleted(filter: (DataBean) -> Boolean) {
        adapter.updateItemWhen({ filter(it) }) {
            adapter.remove(this)
            if (adapter.dataList.isEmpty()) {
                callRefresh(false)
            }
        }
    }

    /**
     * 加载更多
     * @param withAnim 设置是否有上划动画
     */
    fun callLoadMore(withAnim: Boolean = false) {
        if (fetchFromCachedData && !cachedDataList.isNullOrEmpty()) {
            doLoadMoreFromCachedData()
        } else {
            cachedDataList = null
            if (withAnim) {
                smartRefreshLayout?.autoLoadMore()
            } else {
                if (isNetworkConnected(context)) {
                    doLoadMore()
                } else {
                    smartRefreshLayout?.finishLoadMore(false)
                    updateRefreshState(State.NET_LOSE)
                }
            }
        }
    }

    /**
     * 刷新
     * @param withAnim 设置是否有下拉动画
     */
    fun callRefresh(withAnim: Boolean = true) {
        //只有第一次刷新从缓存取数据
        if (isFirstCallRefresh && fetchFromCachedData && (queryRAMCachedData != null || queryDBCachedData != null)) {
            callRefreshFetchCachedData()
        } else {
            fetchFromCachedData = false
            cachedDataList = null
            if (withAnim) {
                smartRefreshLayout?.autoRefresh()
            } else {
                if (isNetworkConnected(context)) {
                    doRefresh()
                } else {
                    smartRefreshLayout?.finishRefresh(false)
                    updateRefreshState(State.NET_LOSE)
                }
            }
        }
    }

    private fun callRefreshFetchCachedData() {
        updateRefreshState(State.LOADING)
        if (queryRAMCachedData != null) {
            queryRAMCachedData()
        } else if (queryDBCachedData != null) {
            queryDBCachedData()
        }
        isFirstCallRefresh = false
    }

    private fun queryRAMCachedData() {
        cachedDataList = queryRAMCachedData?.invoke(this)
        if (cachedDataList.isNullOrEmpty()) {
            fetchFromCachedData = false
            queryDBCachedData()
        } else {
            val splitPage = splitPage(1, dataPerPage, cachedDataList)
            if (splitPage.isNullOrEmpty() && adapter.dataList.isEmpty()) {
                updateRefreshState(State.EMPTY_DATA)
            } else {
                updateRefreshState(State.REFRESH_SUCCESS)
            }
            setData(splitPage)
            smartRefreshLayout?.finishRefresh(true)
        }
    }

    private fun queryDBCachedData() {
        launch(Dispatchers.IO) {
            cachedDataList = queryDBCachedData?.invoke(this)
            if (cachedDataList.isNullOrEmpty()) {
                fetchFromCachedData = false
                launch(Dispatchers.Main) {
                    callRefresh(true)
                }
            } else {
                launch(Dispatchers.Main) {
                    val splitPage = splitPage(1, dataPerPage, cachedDataList)
                    if (splitPage.isNullOrEmpty() && adapter.dataList.isEmpty()) {
                        updateRefreshState(State.EMPTY_DATA)
                    } else {
                        updateRefreshState(State.REFRESH_SUCCESS)
                    }
                    setData(splitPage)
                    smartRefreshLayout?.finishRefresh(true)
                }
            }
        }
    }


    fun launch(context: CoroutineContext = Dispatchers.Main, block: () -> Unit) {
        if (activityOrFragment is ComponentActivity) {
            activityOrFragment.lifecycleScope.launch(context) {
                block()
            }
        } else {
            (activityOrFragment as Fragment).lifecycleScope.launch(context) {
                block()
            }
        }
    }

    private fun doLoadMore() {
        currentPage++
        if (currentPage > totalPage) {
            currentPage = totalPage
            updateRefreshState(State.NO_MORE_DATA_LOADMORE)
            smartRefreshLayout?.finishLoadMoreWithNoMoreData()
            return
        }
        if (requestData == null) {
            smartRefreshLayout?.finishLoadMore(false)
            return
        }
        try {
            loadMore()
        } catch (e: Exception) {
            e.printStackTrace()
            updateRefreshState(State.ERROR)
            smartRefreshLayout?.finishLoadMore(false)
        }
    }


    private fun loadMore() {
        requestData?.invoke(currentPage, dataPerPage) {
            when {
                it.isNullOrEmpty() -> {
                    currentPage--
                    if (adapter.dataList.isEmpty()) {
                        updateRefreshState(State.EMPTY_DATA)
                        smartRefreshLayout?.finishLoadMore(true)
                    } else {
                        updateRefreshState(State.NO_MORE_DATA_LOADMORE)
                        smartRefreshLayout?.finishLoadMoreWithNoMoreData()
                    }
                }
                else -> {
                    if (it.size >= dataPerPage) {
                        updateRefreshState(State.LOAD_MORE_SUCCESS)
                        adapter.addAll(it.toMutableList())
                        smartRefreshLayout?.finishLoadMore(true)
                    } else if (it.size < dataPerPage) {
                        updateRefreshState(State.NO_MORE_DATA_LOADMORE)
                        adapter.addAll(it.toMutableList())
                        smartRefreshLayout?.finishLoadMoreWithNoMoreData()
                    }

                }
            }
        }
    }

    private fun doRefresh() {
        currentPage = 1
        if (requestData == null) return
        updateRefreshState(State.LOADING)
        try {
            refresh()
        } catch (e: Exception) {
            e.printStackTrace()
            updateRefreshState(State.ERROR)
            smartRefreshLayout?.finishRefresh(false)
        }
    }


    private fun refresh() {
        requestData?.invoke(currentPage, dataPerPage) {
            when {
                it.isNullOrEmpty() -> {
                    if (adapter.dataList.isEmpty()) {
                        updateRefreshState(State.EMPTY_DATA)
                    } else {
                        updateRefreshState(State.EMPTY_DATA_ON_REFRESH)
                    }
                    smartRefreshLayout?.finishRefresh()
                }
                else -> {
                    updateRefreshState(State.REFRESH_SUCCESS)
                    adapter.dataList = it.toMutableList()
                    smartRefreshLayout?.finishRefresh()
                }
            }
        }
    }


    private fun updateRefreshState(state: State) {
        if (this.state == state) return
        this.state = state
        coverView?.let {
            stateViewContainer.removeView(it)
            coverView = null
        }
        Log.d(tag, "onStateChange state = " + state.name)
        //默认处理
        when (state) {
            State.LOADING -> stateViewLoading?.let {
                coverView = it
                stateViewContainer.addView(it, -1, -1)
            }
            State.EMPTY_DATA -> stateViewEmptyData?.let {
                coverView = it
                stateViewContainer.addView(it, -1, -1)
            }
            //刷新到空数据，默认清空数据集合，展示空状态。
            State.EMPTY_DATA_ON_REFRESH -> {
                setEmptyState()
            }
            State.NO_MORE_DATA_LOADMORE -> {}
            State.REFRESH_SUCCESS -> {}
            State.LOAD_MORE_SUCCESS -> {}

            State.NET_LOSE -> stateViewNetLose?.let {
                coverView = it
                stateViewContainer.addView(it, -1, -1)
            }

            State.ERROR -> stateViewError?.let {
                coverView = it
                stateViewContainer.addView(it, -1, -1)
            }
            State.NONE -> {}
        }
        //覆盖默认的处理
        onStateChange?.invoke(this, state)
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


    /**
     * 缓存数据分页
     */
    fun splitPage(
        curPage: Int,
        dataPerPage: Int,
        dataList: List<DataBean>?,
    ): List<DataBean>? {
        if (dataList == null) {
            return null
        }
        val start = (curPage - 1) * dataPerPage
        var end = curPage * dataPerPage
        if (end > dataList.size) {
            end = dataList.size - 1
        }
        if (start > end) {
            return null
        }
        return dataList.subList(start, end)

    }


    /**
     * 外部主动设置当前的状态，不会触发 onStateChange
     */
    fun setEmptyState() {
        if (this.state == State.EMPTY_DATA) return
        this.state = State.EMPTY_DATA
        coverView?.let {
            stateViewContainer.removeView(it)
            coverView = null
        }
        adapter.clear()
        stateViewEmptyData?.let {
            coverView = it
            stateViewContainer.addView(it, -1, -1)
        }
    }

    /**
     * 外部主动设置当前的状态，不会触发 onStateChange
     */
    fun setErrorState() {
        if (this.state == State.ERROR) return
        this.state = State.ERROR
        coverView?.let {
            stateViewContainer.removeView(it)
            coverView = null
        }
        adapter.clear()
        stateViewError?.let {
            coverView = it
            stateViewContainer.addView(it, -1, -1)
        }
    }
}