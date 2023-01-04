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
import com.scwang.smart.refresh.layout.SmartRefreshLayout
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
    private val requestData: ((curPage: Int, dataPerPage: Int, callback: (List<DataBean>?) -> Unit) -> Unit)? = null,
) {

    private val TAG = "【RefreshHelper】"

    lateinit var context: Context
        private set

    /**
     * 总页数
     */
    var totalPage: Int = 999


    //recyclerView 的容器；无网络，无数据等视图显示的区域
    lateinit var stateViewContainer: FrameLayout
        private set

    /**
     *  当 state 发生改变时回调。
     *  @see updateRefreshState
     */
    private var onStateChangeListenerList: ArrayList<OnStateChangeListener<DataBean>>? = null


    fun addOnStateChangeListener(listener: OnStateChangeListener<DataBean>) {
        if (onStateChangeListenerList == null) {
            onStateChangeListenerList = arrayListOf()
        }
        onStateChangeListenerList?.add(listener)
    }

    fun removeOnStateChangeListener(listener: OnStateChangeListener<DataBean>) {
        onStateChangeListenerList?.remove(listener)
    }

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
     * true  开启预加载并关闭上拉加载
     * false 关闭预加载并开启上拉加载
     */
    var isEnablePreLoadMore: Boolean = true

    /**
     * 是否正在加载更多
     */
    var isLoadMore = false
        private set

    //开始预加载更多的item剩余个数
    var preLoadMoreItemCount = dataPerPage / 2

    var state: State = State.NONE
        private set

    //当前覆盖在 recyclerView 之上的view
    private var coverView: View? = null

    //覆盖 recyclerView 的状态view
    var stateViewRefreshStart: View? = null
    var stateViewEmptyData: View? = null
    var stateViewNetLose: View? = null
    var stateViewError: View? = null


    init {
        if (activityOrFragment is ComponentActivity) {
            context = activityOrFragment
            Log.d(TAG, "init context is ${activityOrFragment.componentName.className}")
        } else if (activityOrFragment is Fragment) {
            activityOrFragment.context?.let { context = it }
            Log.d(TAG,
                "init context is Fragment , name = ${activityOrFragment.javaClass.simpleName}")
        }

        if (!::context.isInitialized) {
            throw RuntimeException("context is not initialized")
        }

        autoCreateIfNeed()




        recyclerView?.run {
            if (layoutManager == null) {
                layoutManager = LinearLayoutManager(context)
            }
            this.adapter = this@RefreshHelper.adapter
            setHasFixedSize(true)
//            setSupportChangeAnimation(false)
        }


        adapter.onAttachedToRefreshHelper(this)

        smartRefreshLayout?.run {
            setEnableLoadMore(isEnablePullUpLoad)
            setEnableRefresh(isEnablePullDownRefresh)
//            setRefreshHeader(MaterialHeader(context))
//            setRefreshFooter(ClassicsFooter(context).setSpinnerStyle(SpinnerStyle.FixedBehind))
//            setEnableLoadMoreWhenContentNotFull(false)
//            setEnableFooterFollowWhenNoMoreData(true)
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
            Log.d(TAG, "auto create")
            val binding =
                DataBindingUtil.inflate<RefreshHelperLayoutBinding>(LayoutInflater.from(context),
                    R.layout.refresh_helper_layout,
                    null,
                    false)
            smartRefreshLayout = binding.smartRefreshLayout
            recyclerView = binding.recyclerView
            stateViewContainer = binding.stateViewContainer
        } else {
            Log.d(TAG, "user xml create")
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
        stateViewRefreshStart = stateView?.loadingView(context, this)

        stateViewNetLose = stateView?.netLoseView(context, this)

        stateView?.emptyDataView(context, this)?.let { stateViewEmptyData = it }
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

    /**
     * 刷新
     * @param withAnim 设置是否有下拉动画
     */
    fun callRefresh(withAnim: Boolean = true) {
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
        updateRefreshState(State.LOAD_MORE_START)
        currentPage++
        if (currentPage > totalPage) {
            currentPage = totalPage
            updateRefreshState(State.LOAD_MORE_NO_MORE_DATA)
            smartRefreshLayout?.finishLoadMoreWithNoMoreData()
            return
        }
        if (requestData == null) {
            smartRefreshLayout?.finishLoadMore(false)
            isLoadMore = false
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
                        updateRefreshState(State.LOAD_MORE_NO_MORE_DATA)
                        smartRefreshLayout?.finishLoadMoreWithNoMoreData()
                    }
                }
                else -> {
                    if (it.size >= dataPerPage) {
                        adapter.addAll(it.toMutableList())
                        smartRefreshLayout?.finishLoadMore(true)
                        updateRefreshState(State.LOAD_MORE_SUCCESS)
                    } else if (it.size < dataPerPage) {
                        adapter.addAll(it.toMutableList())
                        smartRefreshLayout?.finishLoadMoreWithNoMoreData()
                        updateRefreshState(State.LOAD_MORE_NO_MORE_DATA)
                    }
                }
            }
        }
    }

    private fun doRefresh() {
        updateRefreshState(State.REFRESH_START)
        currentPage = 1
        if (requestData == null) return
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
                    if (it.size >= dataPerPage) {
                        adapter.dataList = it.toMutableList()
                        smartRefreshLayout?.finishRefresh()
                        updateRefreshState(State.REFRESH_SUCCESS)
                    } else if (it.size < dataPerPage) {
                        adapter.dataList = it.toMutableList()
                        smartRefreshLayout?.finishRefreshWithNoMoreData()
                        updateRefreshState(State.REFRESH_NO_MORE_DATA)
                    }
                }
            }
        }
    }


    private fun updateRefreshState(state: State) {
//        Log.d(TAG, "updateRefreshState: state = " +state.name)

        if (this.state == state) return
        this.state = state
        coverView?.let {
            stateViewContainer.removeView(it)
            coverView = null
        }

        //默认处理
        when (state) {
            State.REFRESH_START -> {
                isLoadMore = false
                stateViewRefreshStart?.let {
                    coverView = it
                    stateViewContainer.addView(it, -1, -1)
                }
            }
            State.REFRESH_SUCCESS -> {}

            State.EMPTY_DATA -> stateViewEmptyData?.let {
                coverView = it
                stateViewContainer.addView(it, -1, -1)
            }
            //刷新到空数据，默认清空数据集合，展示空状态。
            State.EMPTY_DATA_ON_REFRESH -> {
                setEmptyState()
            }


            State.LOAD_MORE_START -> {
                isLoadMore = true
            }
            State.LOAD_MORE_SUCCESS -> {
                isLoadMore = false
            }
            State.LOAD_MORE_NO_MORE_DATA -> {
                isLoadMore = false
            }
            State.REFRESH_NO_MORE_DATA -> {
                isLoadMore = false
            }

            State.NET_LOSE -> {
                isLoadMore = false
                stateViewNetLose?.let {
                    coverView = it
                    stateViewContainer.addView(it, -1, -1)
                }
            }

            State.ERROR -> {
                isLoadMore = false
                stateViewError?.let {
                    coverView = it
                    stateViewContainer.addView(it, -1, -1)
                }
            }
            State.NONE -> {}

        }
        Log.d(TAG, "onStateChange: ----------------- " +state.name)
        //覆盖默认的处理
        onStateChangeListenerList?.forEach { it.onStateChange(this, state) }

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
     * 数据分页
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
     * 外部主动设置当前的状态，不会触发 onStateChangeListener
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
     * 外部主动设置当前的状态，不会触发 onStateChangeListener
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