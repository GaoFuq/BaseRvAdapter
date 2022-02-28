package com.gfq.baservadapter.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.gfq.baservadapter.refresh.RVTypeBean
import java.lang.RuntimeException

/**
 *  2022/2/14 16:52
 * @auth gaofuq
 * @description
 */
abstract class BaseRVTypeAdapter<DataBean : RVTypeBean>(
    /**
     * * key : viewType
     * * value : itemLayoutResId
     */
    val viewTypeMap: Map<Int?, Int>,
) : BaseRVAdapter<DataBean>(0) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseVH {
        viewTypeMap.forEach { (viewTypeKey, itemLayoutResId) ->
            if (viewTypeKey == viewType) {
                return BaseVH(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        itemLayoutResId,
                        parent,
                        false
                    )
                )
            }
        }
        throw RuntimeException("BaseRVTypeAdapter onCreateViewHolder error , viewType = $viewType")
    }

    override fun getItemViewType(position: Int): Int {
        return dataList[position].viewType?:0
    }

    override fun onBindView(holder: BaseVH, data: DataBean, position: Int) {
        onBindViewByViewType(holder, data, position, getItemViewType(position))
    }


    abstract fun onBindViewByViewType(
        holder: BaseVH,
        data: DataBean,
        position: Int,
        viewType:Int?
    )

}