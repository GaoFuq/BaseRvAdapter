package com.gfq.baservadapter.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.gfq.baservadapter.refresh.RVSelect


/**
 *  2021/4/13 16:11
 * @auth gaofuq
 * @description
 */
abstract class BaseRVAdapter<DataBean>
    (@LayoutRes private val itemLayoutRes: Int) : RecyclerView.Adapter<BaseVH>() {

    open var dataList = mutableListOf<DataBean>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseVH {
        return BaseVH(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                itemLayoutRes,
                parent,
                false
            )
        )
    }


    override fun onBindViewHolder(holder: BaseVH, position: Int) {
        try {
            holder.vhBinding.executePendingBindings()
            onBindView(holder,
                dataList[holder.bindingAdapterPosition],
                holder.bindingAdapterPosition)
        } catch (e: Exception) {
            Log.e("【BaseRVAdapter】", "onBindViewHolder error ${e.message}")
        }

    }

    override fun onBindViewHolder(holder: BaseVH, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
    }


    protected fun whenPositionLegit(position: Int, block: () -> Unit) {
        if (position >= 0 && position < dataList.size) {
            block()
        } else {
            Log.e("【BaseRVAdapter WARN】", "非法 position  = $position")
        }
    }


    /**
     * 添加一个数据到列表的指定位置。
     * 默认添加到列表最后。
     */
    open fun add(data: DataBean?, positionStart: Int = dataList.size) {
        data ?: return
        if (positionStart >= 0 && positionStart <= dataList.size) {
            dataList.add(positionStart, data)
            notifyItemInserted(positionStart)
        }
    }

    /**
     * 添加数据集合到列表的指定位置
     */
    open fun addAll(list: Collection<DataBean>?, positionStart: Int = dataList.size) {
        if (list.isNullOrEmpty()) return
        if (positionStart >= 0 && positionStart <= dataList.size) {
            if (dataList.addAll(positionStart, list)) {
                notifyItemRangeInserted(positionStart, list.size)
            }
        }
    }


    /**
     * 更新指定位置的数据。
     * 使用一个新的数据替换该位置的原数据。
     */
    fun updateItem(position: Int, data: DataBean) {
        whenPositionLegit(position) {
            dataList[position] = data
            notifyItemChanged(position,"changed")
        }
    }


    /**
     * 更新指定位置的数据。
     * 修改该位置的原数据。
     */
    fun updateItem(position: Int, update: DataBean.() -> Unit) {
        whenPositionLegit(position) {
            update(dataList[position])
            notifyItemChanged(position,"changed")
        }
    }


    /**
     * 更新特定条件的数据集合。
     * updateData({it==null}){//doUpdate}
     */
    fun updateItemWhen(filter: (DataBean) -> Boolean, update: DataBean.() -> Unit) {
        dataList.filter { filter(it) }.forEach {
            val position = dataList.indexOf(it)
            updateItem(position, update)
        }
    }


    open fun removeAt(position: Int): DataBean? {
        var temp: DataBean? = null
        whenPositionLegit(position) {
            Log.d("【BaseRVAdapter】", "removeAt position = $position")
            temp = dataList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, dataList.size - position)
        }
        return temp
    }

    open fun remove(data: DataBean?): DataBean? {
        var temp: DataBean? = null
        data?.let {
            temp = removeAt(dataList.indexOf(data))
        }
        return temp
    }

    @SuppressLint("NotifyDataSetChanged")
    open fun clear() {
        dataList.clear()
        notifyDataSetChanged()
    }


    abstract fun onBindView(holder: BaseVH, data: DataBean, position: Int)

    override fun getItemCount(): Int = dataList.size

}