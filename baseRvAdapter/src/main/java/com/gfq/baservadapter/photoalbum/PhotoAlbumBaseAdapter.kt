package com.gfq.baservadapter.photoalbum

import android.app.Activity
import android.content.Intent
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.gfq.baservadapter.adapter.BaseRVAdapter
import com.gfq.baservadapter.adapter.BaseVH

/**
 *  2022/1/24 10:07
 * @auth gaofuq
 */


/**
 * * 实现 [PhotoAlbum.open]
 * * 实现 [PhotoAlbum.getResult]
 */
abstract class PhotoAlbumBaseAdapter(
    itemLayoutRes: Int,
    private val maxSelectNum: Int = 9,
    private val order: Order = Order.inOrder,
) : BaseRVAdapter<String>(itemLayoutRes), PhotoAlbum {


    //两个方法中每个view的绑定都要覆写，否则重用机制会导致数据混乱有错误。
    abstract fun onBindAdder(holder: BaseVH, data: String)
    abstract fun onBind(holder: BaseVH, data: String, position: Int)


    override fun onBindView(holder: BaseVH, data: String, position: Int) {
        if (position == dataList.lastIndex) {
            onBindAdder(holder, data)
        } else {
            onBind(holder, data, position)
        }

        if (position == maxSelectNum) {
            holder.itemView.isGone = true
        } else {
            holder.itemView.isVisible = true
        }
    }

    enum class Order {
        //先后顺序
        inOrder,

        //先后顺序的倒序
        reverseOrder
    }

    fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
    ): List<String> {
        val list = mutableListOf<String>()
        if (resultCode == Activity.RESULT_OK) {
            val tempList = getResult(requestCode, resultCode, data).toMutableList()
            if(tempList.isEmpty())return emptyList()
            list.addAll(tempList)
            when (order) {
                Order.inOrder -> {
                    dataList.removeAt(dataList.lastIndex)
                    dataList.addAll(tempList)
                    dataList.add("")
                    notifyDataSetChanged()
                }
                Order.reverseOrder -> {
                    tempList.addAll(dataList)
                    clear()
                    addAll(tempList)
                }
            }

        }
        return list
    }
}