package com.gfq.baservadapter.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.gfq.baservadapter.refresh.RVSelectBean


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


    protected fun whenDataIsRVSelectBean(data: DataBean, block: (RVSelectBean) -> Unit) {
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

    fun addAll(list: Collection<DataBean>, positionStart: Int = -1) {
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

    fun setData(position: Int, data: DataBean) {
        whenPositionLegit(position) {
            dataList[position] = data
            notifyItemChanged(position)
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
            whenDataIsRVSelectBean(dataList[position]) { lastSelectedPosition = -1 }
            dataList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, dataList.size - position)
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



    open fun onItemSelected(holder: BaseVH, data: DataBean, position: Int) {}

    open fun onItemNotSelect(holder: BaseVH, data: DataBean, position: Int) {}

    open fun onItemReSelect(holder: BaseVH, data: DataBean, position: Int) {}


}