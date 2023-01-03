package com.gfq.baservadapter.adapter

import androidx.annotation.LayoutRes
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
 * 适用于 单类型 的布局。
 * 默认处理了 GridLayoutManager 的情况。
 *
 * //1.定义一个全局通用的 FooterAdapter，实现Footer样式。
 * //2.需要 Footer 的直接继承 FooterAdapter。
 *
abstract class RVFooterAdapter<T:RVFooter>(layoutId:Int):BaseRVFooterAdapter<T>(layoutId) {
    override fun initFooterLayout(binding: RefreshFooterLayoutBinding) {
        binding.footerRoot.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            height=binding.footerRoot.dp(R.dimen.dp50)
        }
        binding.textView.text="我就是底线啊！！！！"
        binding.textView.setTextColor(Color.RED)
    }
}
 */

abstract class BaseRVFooterAdapter<DataBean : RVFooter>
    (@LayoutRes private val itemLayoutRes: Int) :
    BaseRVTypeFooterAdapter<DataBean>(mapOf(R.id.refresh_body_layout to itemLayoutRes)) {


    abstract fun onBindBodyView(holder: BaseVH, data: DataBean, position: Int)

    override fun onBindBodyViewByViewType(
        holder: BaseVH,
        data: DataBean,
        position: Int,
        viewType: Int?,
    ) {
        when (viewType) {
            R.id.refresh_body_layout -> onBindBodyView(holder, data, position)
        }
    }
}