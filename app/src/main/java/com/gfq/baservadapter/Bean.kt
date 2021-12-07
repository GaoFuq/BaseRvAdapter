package com.gfq.baservadapter

import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding


/**
 *  2021/6/2 14:41
 * @auth gaofuq
 * @description
 */
/**
 * 继承 RVSelectBean 使其具有 select 属性
 */
open class RVSelectBean(open var select: Boolean = false)

data class ViewTypeWrapper(@LayoutRes val viewTypeLayout: Int, val viewType: Int)

inline fun <reified T : ViewDataBinding> BaseVH.to(): T {
    return this.vhBinding as T
}
