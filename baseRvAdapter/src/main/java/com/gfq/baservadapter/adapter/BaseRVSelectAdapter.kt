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


    var lastSingleSelectedPosition: Int? = null
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
        lastSingleSelectedPosition?.let {
            if (it >= 0) {
                if (it > positionStart) {
                    lastSingleSelectedPosition = it + 1
                }
            }
        }

    }

    override fun addAll(list: Collection<DataBean>?, positionStart: Int) {
        super.addAll(list, positionStart)
        list ?: return
        lastSingleSelectedPosition?.let {
            if (it >= 0) {
                if (it >= positionStart) {
                    lastSingleSelectedPosition = it + list.size
                }
            }
        }

    }

    override fun removeAt(position: Int): DataBean? {
        val result = super.removeAt(position)
        whenPositionLegit(position) {
            lastSingleSelectedPosition = null
        }
        return result
    }

    override fun clear() {
        super.clear()
        lastSingleSelectedPosition = null
    }

    /**
     * 单选
     */
    fun doSingleSelect(holder: BaseVH?, position: Int) {
        if (dataList.isEmpty()) return

        whenPositionLegit(position) {
            if (lastSingleSelectedPosition == position) {
                setItemReSelect(holder, position)
            } else {
                setItemSelected(position)
                lastSingleSelectedPosition?.let { setItemCancelSelect(it) }
            }
            lastSingleSelectedPosition = position
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


    private fun setItemReSelect(holder: BaseVH?, position: Int) {
        whenPositionLegit(position) {
            Log.d("【BaseRVAdapter】", "setItemReSelect position = $position")
            onItemReSelect(holder, dataList[position], position)
        }
    }

    private fun setItemSelected(position: Int) {
        whenPositionLegit(position) {
            Log.d("【BaseRVAdapter】", "setItemSelected position = $position")
            dataList[position].select = true
            notifyItemChanged(position, "selectChanged")
            lastSingleSelectedPosition = position
        }
    }


    fun setItemCancelSelect(position: Int) {
        whenPositionLegit(position) {
            Log.d("【BaseRVAdapter】", "setItemCancelSelect position = $position")
            dataList[position].select = false
            notifyItemChanged(position, "selectChanged")
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