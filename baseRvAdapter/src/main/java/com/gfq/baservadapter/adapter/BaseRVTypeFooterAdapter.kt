package com.gfq.baservadapter.adapter

import androidx.core.view.doOnLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.gfq.baservadapter.R
import com.gfq.baservadapter.databinding.RefreshFooterLayoutBinding
import com.gfq.baservadapter.refresh.*

/**
 *  2023/1/3 16:52
 * @auth gaofuq
 * @description
 * 搭配[RefreshHelper]，带有 Footer 的 Adapter。
 * 在刷新无更多数据 或 加载更多无更多数据时，显示Footer。
 * 适用于 多类型 的布局。
 * 默认处理了 GridLayoutManager 的情况。
 *
 * 1.定义一个全局通用的 TypeFooterAdapter，实现Footer样式。
 * 2.需要 Footer 的直接继承 TypeFooterAdapter。
 * 3.子类按需实现：[BaseRVTypeFooterAdapter.getSpanSizeWhenMultiViewType]
 *
abstract class RVTypeFooterAdapter<T:RVFooter>(map:Map<Int?,Int>): BaseRVTypeFooterAdapter<T>(map) {
    override fun initFooterLayout(binding: RefreshFooterLayoutBinding) {
        binding.footerRoot.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            height=binding.footerRoot.dp(R.dimen.dp50)
        }
        binding.textView.text="我就是底线啊！！！！"
        binding.textView.setTextColor(Color.RED)
    }
}
 */
abstract class BaseRVTypeFooterAdapter<DataBean : RVFooter>
    (
    /**
     * * key : viewType
     * * value : itemLayoutResId
     */
    viewTypeMap: Map<Int?, Int>,
) : BaseRVTypeAdapter<DataBean>(
    viewTypeMap + mapOf(R.id.refresh_footer_layout to R.layout.refresh_footer_layout)
) {

    override fun onBindViewByViewType(
        holder: BaseVH,
        data: DataBean,
        position: Int,
        viewType: Int?,
    ) {
        when (viewType) {
            R.id.refresh_footer_layout -> initFooterLayout(holder.get())
            else -> onBindBodyViewByViewType(holder, data, position,viewType)
        }

    }

    abstract fun initFooterLayout(binding: RefreshFooterLayoutBinding)

    abstract fun onBindBodyViewByViewType(holder: BaseVH, data: DataBean, position: Int, viewType: Int?)

    open fun getSpanSizeWhenMultiViewType(position: Int):Int = 1

    override fun onAttachedToRefreshHelper(helper: RefreshHelper<DataBean>) {
        super.onAttachedToRefreshHelper(helper)
        helper.recyclerView?.doOnLayout {
            val layoutManager = helper.recyclerView?.layoutManager
            if (layoutManager is GridLayoutManager) {
                layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        if (position == helper.adapter.dataList.lastIndex) {
                            return layoutManager.spanCount
                        } else {
                            return getSpanSizeWhenMultiViewType(position)
                        }
                    }
                }
            }
        }

        helper.addOnStateChangeListener(object : OnStateChangeListener<DataBean> {
            override fun onStateChange(helper: RefreshHelper<DataBean>, state: State) {
                when (state) {
                    State.LOAD_MORE_NO_MORE_DATA,
                    State.REFRESH_NO_MORE_DATA,
                    -> {
                        if (dataList.lastOrNull()?.isFooter == false) {
                            add(RVFooter(true, viewType = R.id.refresh_footer_layout) as? DataBean)
                        }
                    }
                    else -> {
                        if (dataList.lastOrNull()?.isFooter == true) {
                            removeAt(dataList.lastIndex)
                        }
                    }
                }
            }
        })
    }
}