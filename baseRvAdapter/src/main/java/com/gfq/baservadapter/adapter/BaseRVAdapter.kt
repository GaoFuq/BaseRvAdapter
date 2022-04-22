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
            onBindView(holder, dataList[position], position)

            if (dataList[position] is RVSelect) {
                val data = dataList[position] as RVSelect
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

    override fun onBindViewHolder(holder: BaseVH, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
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
        maxSelectCount: Int = -1,
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


    protected fun whenPositionLegit(position: Int, block: () -> Unit) {
        if (position >= 0 && position < dataList.size) {
            block()
        } else {
            Log.w("【BaseRVAdapter WARN】", "position 非法 = $position")
        }
    }

    protected fun whenAddDataPositionLegit(position: Int, block: () -> Unit) {
        if (position >= 0 && position <= dataList.size) {
            block()
        } else {
            Log.w("【BaseRVAdapter WARN】", "position 非法 = $position")
        }
    }


    protected fun whenDataIsRVSelectBean(data: DataBean, block: (RVSelect) -> Unit) {
        if (data is RVSelect) {
            block(data as RVSelect)
        } else {
            Log.e("【BaseRVAdapter】", "${data!!::class.java.name} require extends RVSelectBean")
        }
    }


    fun getMultipleSelectedCount() = getMultipleSelectDataList().count()

    fun getMultipleSelectDataList(): List<DataBean> =
        dataList.filter { (it is RVSelect && it.select) }

    fun getSingleSelectData(): DataBean? =
        dataList.firstOrNull { (it is RVSelect && it.select) }


    /**
     * 添加一个数据到列表的指定位置。
     * 默认添加到列表最后。
     */
    fun add(data: DataBean, positionStart: Int = -1) {
        val posStart = if (positionStart < 0) {
            dataList.size
        } else {
            positionStart
        }
        whenAddDataPositionLegit(posStart) {
            dataList.add(posStart, data)
            notifyItemInserted(posStart)

            if (lastSelectedPosition >= 0) {
                if (lastSelectedPosition > posStart) {
                    lastSelectedPosition++
                }
            }
        }
    }

    /**
     * 添加数据集合到列表的指定位置
     */
    fun addAll(list: Collection<DataBean>, positionStart: Int = -1) {
        if (list.isEmpty()) return
        val posStart = if (positionStart < 0) {
            dataList.size
        } else {
            positionStart
        }
        whenAddDataPositionLegit(posStart) {
            if (dataList.addAll(posStart, list)) {
                notifyItemRangeInserted(posStart, list.size)

                if (lastSelectedPosition >= 0) {
                    if (lastSelectedPosition >= posStart) {
                        lastSelectedPosition += list.size
                    }
                }
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
            notifyItemChanged(position)
        }
    }


    /**
     * 更新指定位置的数据。
     * 修改该位置的原数据。
     */
    fun updateItem(position: Int,update: DataBean.() -> Unit) {
        whenPositionLegit(position) {
            update(dataList[position])
            notifyItemChanged(position)
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


    fun removeAt(position: Int): DataBean? {
        var temp: DataBean? = null
        whenPositionLegit(position) {
            Log.d("【BaseRVAdapter】", "removeAt position = $position")
            whenDataIsRVSelectBean(dataList[position]) { lastSelectedPosition = -1 }
            temp = dataList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, dataList.size - position)

            if (lastSelectedPosition >= 0) {
                if (lastSelectedPosition > position) {
                    lastSelectedPosition--
                }
                if (lastSelectedPosition == position) {
                    lastSelectedPosition = -1
                }
            }
        }
        return temp
    }

    fun remove(data: DataBean?): DataBean? {
        var temp: DataBean? = null
        data?.let {
            temp = removeAt(dataList.indexOf(data))
        }
        return temp
    }

    fun clear() {
        dataList.clear()
        notifyDataSetChanged()
        lastSelectedPosition = -1
    }


    abstract fun onBindView(holder: BaseVH, data: DataBean, position: Int)

    override fun getItemCount(): Int = dataList.size


    open fun onItemSelected(holder: BaseVH, data: DataBean, position: Int) {}

    open fun onItemNotSelect(holder: BaseVH, data: DataBean, position: Int) {}

    open fun onItemReSelect(holder: BaseVH, data: DataBean, position: Int) {}


}