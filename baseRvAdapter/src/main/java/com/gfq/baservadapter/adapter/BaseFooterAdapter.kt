package com.gfq.baservadapter.adapter

import androidx.annotation.LayoutRes
import com.gfq.baservadapter.R
import com.gfq.baservadapter.databinding.RefreshFooterLayoutBinding
import com.gfq.baservadapter.refresh.*

/**
 *  2023/1/3 16:52
 * @auth gaofuq
 * @description
 */
abstract class BaseFooterAdapter<DataBean : RVFooter>
    (@LayoutRes private val itemLayoutRes: Int) :
    BaseRVTypeAdapter<DataBean>(mapOf(
        R.id.refresh_body_layout to itemLayoutRes,
        R.id.refresh_footer_layout to R.layout.refresh_footer_layout
    )) {

    override fun onBindViewByViewType(
        holder: BaseVH,
        data: DataBean,
        position: Int,
        viewType: Int?,
    ) {
        initFooterLayout(holder.get())
        onBindBodyView(holder, data, position)
    }

    abstract fun initFooterLayout(binding: RefreshFooterLayoutBinding)

    abstract fun onBindBodyView(holder: BaseVH, data: DataBean, position: Int)


    override fun onAttachedToRefreshHelper(helper: RefreshHelper<DataBean>) {
        super.onAttachedToRefreshHelper(helper)

        helper.addOnStateChangeListener(object : OnStateChangeListener<DataBean> {
            override fun onStateChange(helper: RefreshHelper<DataBean>, state: State) {
                when (state) {
                    State.LOAD_MORE_NO_MORE_DATA,
                    State.REFRESH_NO_MORE_DATA,
                    -> {
                        if (!dataList.last().isFooter) {
                            add(RVFooter(true, viewType = R.id.refresh_footer_layout) as? DataBean)
                        }
                    }
                    else -> {
                        if (dataList.last().isFooter) {
                            removeAt(dataList.lastIndex)
                        }
                    }
                }
            }
        })
    }

    override fun getItemCount(): Int {
        return if (
            refreshHelper?.state == State.REFRESH_NO_MORE_DATA
            || refreshHelper?.state == State.LOAD_MORE_NO_MORE_DATA
        ) {
            super.getItemCount() + 1
        } else {
            super.getItemCount()
        }
    }
}