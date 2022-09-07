package com.gfq.baservadapter.adapter

import android.annotation.SuppressLint
import android.util.Log
import androidx.annotation.LayoutRes
import com.gfq.baservadapter.refresh.RVSelect

/**
 *  2022/6/23 14:23
 * @auth gaofuq
 * @description
 */
abstract class BaseRVSelectAdapter<DataBean : RVSelect>(@LayoutRes private val itemLayoutRes: Int) :
    BaseRVAdapter<DataBean>(itemLayoutRes) {

    val NO_POSITION = -1

    var lastSelectedPosition = NO_POSITION
        private set


    override fun onBindViewHolder(holder: BaseVH, position: Int) {
        super.onBindViewHolder(holder, position)

        val data = dataList[position]
        if (data.select) {
            onItemSelected(holder, dataList[position], position)
        } else {
            onItemNotSelect(holder, dataList[position], position)
        }
    }


    override fun add(data: DataBean?, positionStart: Int) {
        super.add(data, positionStart)
        if (lastSelectedPosition >= 0) {
            if (lastSelectedPosition > positionStart) {
                lastSelectedPosition++
            }
        }
    }

    override fun addAll(list: Collection<DataBean>?, positionStart: Int) {
        super.addAll(list, positionStart)
        list ?: return
        if (lastSelectedPosition >= 0) {
            if (lastSelectedPosition >= positionStart) {
                lastSelectedPosition += list.size
            }
        }
    }

    override fun removeAt(position: Int): DataBean? {
        val result = super.removeAt(position)
        whenPositionLegit(position) {
            lastSelectedPosition = NO_POSITION
        }
        return result
    }

    override fun clear() {
        super.clear()
        lastSelectedPosition = NO_POSITION
    }

    /**
     * 单选
     */
    fun doSingleSelect(holder: BaseVH?, position: Int) {
        if (dataList.isEmpty()) return

        whenPositionLegit(position) {
            if (lastSelectedPosition == position) {
                setItemReSelect(holder, position)
            } else {
                setItemSelected(position)
                setItemCancelSelect(lastSelectedPosition)
            }
            lastSelectedPosition = position
        }
    }

    /**
     * 多选
     */
    fun doMultipleSelect(
        holder: BaseVH?,
        position: Int,
        maxSelectCount: Int = -1,
        onCountOverMax: (() -> Unit)? = null,
        interceptReSelect: Boolean = false,
    ) {
        if (dataList.isEmpty()) return

        whenPositionLegit(position) {
            if (dataList[position].select) {
                if (interceptReSelect) {
                    setItemReSelect(holder, position)
                } else {
                    setItemCancelSelect(position)
                }
            } else {
                if (maxSelectCount <= 0) {
                    setItemSelected(position)
                } else {
                    if (getMultipleSelectedCount() >= maxSelectCount) {
                        onCountOverMax?.invoke()
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
            dataBean.select = true
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
            dataBean.select = !dataBean.select
        }
        notifyDataSetChanged()
    }


    fun setItemReSelect(holder: BaseVH?, position: Int) {
        whenPositionLegit(position) {
            Log.d("【BaseRVAdapter】", "setItemReSelect position = $position")
            onItemReSelect(holder, dataList[position], position)
        }

    }

    fun setItemSelected(position: Int) {
        whenPositionLegit(position) {
            Log.d("【BaseRVAdapter】", "setItemSelected position = $position")
            dataList[position].select = true
            notifyItemChanged(position,"selectChanged")
        }
    }


    fun setItemCancelSelect(position: Int) {
        whenPositionLegit(position) {
            Log.d("【BaseRVAdapter】", "setItemCancelSelect position = $position")
            dataList[position].select = false
            notifyItemChanged(position,"selectChanged")
        }
    }

    fun getMultipleSelectedCount() = getMultipleSelectDataList().count()

    fun getMultipleSelectDataList(): List<DataBean> =
        dataList.filter { it.select }

    fun getSingleSelectData(): DataBean? =
        dataList.firstOrNull { it.select }


    open fun onItemSelected(holder: BaseVH?, data: DataBean, position: Int) {}

    open fun onItemNotSelect(holder: BaseVH?, data: DataBean, position: Int) {}

    open fun onItemReSelect(holder: BaseVH?, data: DataBean, position: Int) {}

}