package com.gfq.baservadapter

import androidx.recyclerview.widget.DiffUtil

/**
 *  2021/12/3 17:58
 * @auth gaofuq
 * @description
 */
class BaseDiffCallback<DataBean> : DiffUtil.Callback() {
    var oldDataList: MutableList<DataBean>? = null
    var newDataList: MutableList<DataBean>? = null


    override fun getOldListSize(): Int = oldDataList?.size ?: 0

    override fun getNewListSize(): Int = newDataList?.size ?: 0

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        if (oldDataList != null && newDataList != null) {
            return oldDataList!![oldItemPosition] === newDataList!![newItemPosition]
        }
        return false
    }

    override fun areContentsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        if (oldDataList != null && newDataList != null) {
            return oldDataList!![oldItemPosition] == newDataList!![newItemPosition]
        }
        return false
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        return super.getChangePayload(oldItemPosition, newItemPosition)
    }
}