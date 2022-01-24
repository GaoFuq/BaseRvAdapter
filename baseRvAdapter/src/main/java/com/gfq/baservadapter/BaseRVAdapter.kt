package com.gfq.baservadapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator


/**
 *  2021/4/13 16:11
 * @auth gaofuq
 * @description
 */
abstract class BaseRVAdapter<DataBean>(
    @LayoutRes private val itemLayoutRes: Int,
    private val viewTypeWrappers: List<ViewTypeWrapper>? = null,
) : RecyclerView.Adapter<BaseVH>() {

    var recyclerView: RecyclerView? = null
        set(value) {
            if (field == null) {
                field = value
                field?.adapter = this
                val amt = field?.itemAnimator
                if (amt != null && amt is SimpleItemAnimator) {
                    amt.supportsChangeAnimations = false
                }
            }
        }

    open var dataList = mutableListOf<DataBean>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseVH {
        if (viewTypeWrappers != null) {
            viewTypeWrappers.forEach {
                if (viewType == it.viewType) {
                    return BaseVH(
                        DataBindingUtil.inflate(
                            LayoutInflater.from(parent.context),
                            it.viewTypeLayout,
                            parent,
                            false
                        )
                    )
                }
            }
            throw RuntimeException("useViewType == true ,but can not impl")
        } else {
            return BaseVH(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    itemLayoutRes,
                    parent,
                    false
                )
            )
        }
    }


    override fun onBindViewHolder(holder: BaseVH, position: Int) {
        if (viewTypeWrappers != null) {
            onBindViewByViewType(holder, dataList[position], position, viewTypeWrappers)
        } else {
            try {
                holder.vhBinding.executePendingBindings()
                onBindView(holder, dataList[position], position)

                if (dataList[position] is RVSelectBean) {
                    val data = dataList[position] as RVSelectBean
                    if (data.select) {
                        onItemSelected(holder, dataList[position], position)
                    } else {
                        onItemNotSelect(holder, dataList[position], position)
                    }
                }
            } catch (e: Exception) {
                Log.e("【BaseRVAdapter】", "onBindViewHolder error ${e.message}")
            }
        }
    }

    override fun onBindViewHolder(holder: BaseVH, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
    }

    /**
     * 多item布局时，重写该方法
     */
    open fun onBindViewByViewType(
        holder: BaseVH,
        data: DataBean,
        position: Int,
        viewTypeWrappers: List<ViewTypeWrapper>,
    ) {

    }

    var lastSelectedPosition = -1
        private set


    /**
     * 单选
     */
    fun doSingleSelect(holder: BaseVH, position: Int) {
        if (dataList.isEmpty()) return

        whenPositionLegit(position) {
            whenDataIsRVSelectBean(dataList[position]) {
                if (lastSelectedPosition == position) {
                    setItemReSelect(holder, position)
                } else {
                    setItemSelected(position)
                    setItemCancelSelect(lastSelectedPosition)
                }
                lastSelectedPosition = position
            }
        }
    }

    /**
     * 多选
     */
    fun doMultipleSelect(
        holder: BaseVH,
        position: Int,
        maxSelectCount: Int? = null,
        onCountOverMax: (() -> Unit)? = null,
        interceptReSelect: Boolean = false,
    ) {
        if (dataList.isEmpty()) return

        whenPositionLegit(position) {
            whenDataIsRVSelectBean(dataList[position]) {
                if (it.select) {
                    if (interceptReSelect) {
                        setItemReSelect(holder, position)
                    } else {
                        setItemCancelSelect(position)
                    }
                } else {
                    maxSelectCount?.let {
                        if (getMultipleSelectedCount() >= maxSelectCount) {
                            onCountOverMax?.invoke()
                        } else {
                            setItemSelected(position)
                        }
                    }
                }
            }
        }

    }

    /**
     * 全选
     */
    @SuppressLint("NotifyDataSetChanged")
    fun doAllSelect() {
        if (dataList.isEmpty()) return
        dataList.forEach { dataBean ->
            whenDataIsRVSelectBean(dataBean) {
                it.select = true
            }
        }
        notifyDataSetChanged()
    }

    /**
     * 反选
     */
    @SuppressLint("NotifyDataSetChanged")
    fun doReverseSelect() {
        if (dataList.isEmpty()) return
        dataList.forEach { dataBean ->
            whenDataIsRVSelectBean(dataBean) {
                it.select = !it.select
            }
        }
        notifyDataSetChanged()
    }


    fun setItemReSelect(holder: BaseVH, position: Int) {
        whenPositionLegit(position) {
            whenDataIsRVSelectBean(dataList[position]) {
                Log.d("【BaseRVAdapter】", "setItemReSelect position = $position")
                onItemReSelect(holder, dataList[position], position)
            }
        }

    }

    fun setItemSelected(position: Int) {
        whenPositionLegit(position) {
            whenDataIsRVSelectBean(dataList[position]) {
                Log.d("【BaseRVAdapter】", "setItemSelected position = $position")
                it.select = true
                notifyItemChanged(position)
            }

        }
    }


    fun setItemCancelSelect(position: Int) {
        whenPositionLegit(position) {
            whenDataIsRVSelectBean(dataList[position]) {
                Log.d("【BaseRVAdapter】", "setItemCancelSelect position = $position")
                it.select = false
                notifyItemChanged(position)
            }
        }
    }


    private fun whenPositionLegit(position: Int, block: () -> Unit) {
        if (position >= 0 && position < dataList.size) {
            block()
        } else {
            Log.w("【BaseRVAdapter WARN】", "position 非法 = $position")
        }
    }

    private fun whenAddDataPositionLegit(position: Int, block: () -> Unit) {
        if (position >= 0 && position <= dataList.size) {
            block()
        } else {
            Log.w("【BaseRVAdapter WARN】", "position 非法 = $position")
        }
    }


    private fun whenDataIsRVSelectBean(data: DataBean, block: (RVSelectBean) -> Unit) {
        if (data is RVSelectBean) {
            block(data as RVSelectBean)
        } else {
            Log.e("【BaseRVAdapter】", "${data!!::class.java.name} require extends RVSelectBean")
        }
    }


    fun getMultipleSelectedCount() = getMultipleSelectDataList().count()

    fun getMultipleSelectDataList(): List<DataBean> =
        dataList.filter { (it is RVSelectBean && it.select) }

    fun getSingleSelectData(): DataBean? =
        dataList.firstOrNull { (it is RVSelectBean && it.select) }

    fun addAll(list: List<DataBean>, positionStart: Int = -1) {
        val posStart = if (positionStart < 0) {
            dataList.size
        } else {
            positionStart
        }
        whenAddDataPositionLegit(posStart) {
            if (dataList.addAll(list)) {
                notifyItemRangeInserted(posStart, list.size)
            }
        }
    }

    fun add(data: DataBean, positionStart: Int = -1) {
        val posStart = if (positionStart < 0) {
            dataList.size
        } else {
            positionStart
        }
        whenAddDataPositionLegit(posStart) {
            dataList.add(data)
            notifyItemInserted(posStart)
        }
    }


    fun removeAt(position: Int) {
        whenPositionLegit(position) {
            Log.d("【BaseRVAdapter】", "removeAt position = $position")
            dataList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, dataList.size - position)
            whenDataIsRVSelectBean(dataList[position]) { lastSelectedPosition = -1 }
        }
    }

    fun remove(data: DataBean?) {
        data?.let {
            removeAt(dataList.indexOf(data))
        }
    }

    fun clear() {
        dataList.clear()
        notifyDataSetChanged()
        lastSelectedPosition = -1
    }


    abstract fun onBindView(holder: BaseVH, data: DataBean, position: Int)

    override fun getItemCount(): Int = dataList.size


    override fun getItemViewType(position: Int): Int {
        return if (viewTypeWrappers != null) {
            viewTypeWrappers[position].viewTypeLayout
        } else {
            super.getItemViewType(position)
        }
    }


    open fun onItemSelected(holder: BaseVH, data: DataBean, position: Int) {}

    open fun onItemNotSelect(holder: BaseVH, data: DataBean, position: Int) {}

    open fun onItemReSelect(holder: BaseVH, data: DataBean, position: Int) {}


}