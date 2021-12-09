package com.gfq.baservadapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView


/**
 *  2021/4/13 16:11
 * @auth gaofuq
 * @description
 */
abstract class BaseRVAdapter<DataBean>(
    @LayoutRes private val itemLayoutRes: Int,
    private val useViewType: Boolean = false,
    private val viewTypeWrappers: List<ViewTypeWrapper>? = null,
) : RecyclerView.Adapter<BaseVH>() {

    var dataList = mutableListOf<DataBean>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseVH {
        if (useViewType) {
            viewTypeWrappers?.forEach {
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
        if (useViewType) {
            viewTypeWrappers?.let {
                onBindViewByViewType(holder, dataList[position], position, it)
            }
        } else {
            try {
                holder.vhBinding.executePendingBindings()
                onBindView(holder, dataList[position], position)

                if (dataList[position] is RVSelectBean) {
                    val data = dataList[position] as RVSelectBean
                    if (data.select) {
                        doOnSelect(holder, dataList[position], position)
                    } else {
                        doUnSelect(holder, dataList[position], position)
                    }
                }
            } catch (e: Exception) {
                Log.e("【ERROR】", "onBindViewHolder ${e.message}")
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

    var lastSingleSelectPosition = -1
        private set

    var lastMultipleSelectPosition = -1
        private set


    /**
     * @param onReSelectListener 重复选择的处理逻辑，默认不做处理
     */
    fun onSingleSelectClick(position: Int, onReSelectListener: OnReSelectListener<DataBean>? = null) {
        if (dataList.isEmpty()) return

        whenPositionLegit(position) {
            if (dataList[position] is RVSelectBean) {
                val b = dataList[position] as RVSelectBean
                b.select = true
                notifyItemChanged(position)
            }
        }



        if (lastSingleSelectPosition == position) {
            if (onReSelectListener == null) {
                //默认处理
                //不做处理
            } else {
                //自己处理
                onReSelectListener.onReSelect(position,this)
            }
        } else {
            whenPositionLegit(lastSingleSelectPosition) {
                if (dataList[lastSingleSelectPosition] is RVSelectBean) {
                    (dataList[lastSingleSelectPosition] as RVSelectBean).select = false
                    notifyItemChanged(lastSingleSelectPosition)
                }
            }
        }

        lastSingleSelectPosition = position
    }

    private fun whenPositionLegit(position: Int, block: () -> Unit) {
        if (position >= 0 && position < dataList.size) {
            block()
        } else {
            Log.e("【BaseRVAdapter ERROR】", "position 非法 = $position")
        }
    }


    fun onMultipleSelectClick(
        position: Int,
        minSelectCount: Int? = null,
        maxSelectCount: Int? = null,
        onCountOverMax: (() -> Unit)? = null,
        onCountLessMin: (() -> Unit)? = null,
        onReSelectListener: OnReSelectListener<DataBean>? = null
    ) {
        if (dataList.isEmpty()) return

        minSelectCount?.let {
            if (getMultipleSelectedCount() < it) {
                onCountLessMin?.invoke()
                return
            }
        }

        maxSelectCount?.let {
            if (getMultipleSelectedCount() > it) {
                onCountOverMax?.invoke()
                return
            }
        }

        if (lastMultipleSelectPosition == position) {
            if (onReSelectListener == null) {
                //默认处理
                //不做处理
            } else {
                //自己处理
                onReSelectListener.onReSelect(position,this)
            }
        } else {
            whenPositionLegit(position) {
                if (dataList[position] is RVSelectBean) {
                    val b = dataList[position] as RVSelectBean
                    b.select = !b.select
                }
                notifyItemChanged(position)
            }
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
        whenPositionLegit(posStart) {
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
        whenPositionLegit(posStart) {
            dataList.add(data)
            notifyItemInserted(posStart)
        }

    }


    fun removeAt(position: Int) {
        whenPositionLegit(position) {
            dataList.removeAt(position)
            notifyItemRemoved(position)
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
    }


    abstract fun onBindView(holder: BaseVH, data: DataBean, position: Int)

    override fun getItemCount(): Int = dataList.size


    override fun getItemViewType(position: Int): Int {
        return if (useViewType) {
            if (viewTypeWrappers != null) {
                viewTypeWrappers[position].viewTypeLayout
            } else {
                super.getItemViewType(position)
            }
        } else {
            super.getItemViewType(position)
        }
    }


    open fun doOnSelect(holder: BaseVH, data: DataBean, position: Int) {}

    open fun doUnSelect(holder: BaseVH, data: DataBean, position: Int) {}
}