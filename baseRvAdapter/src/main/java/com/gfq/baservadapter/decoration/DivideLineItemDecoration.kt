package com.gfq.baservadapter.decoration

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 *  2022/7/25 16:18
 * @auth gaofuq
 * @description
 */
class DivideLineItemDecoration(
    private val lineHeight: Float = 2f,
    private val leftMargin: Float = 0f,
    private val rightMargin: Float = 0f,
    private val lineColor:Int = Color.GRAY
) : RecyclerView.ItemDecoration() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = lineColor }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        val pos = parent.getChildAdapterPosition(view)
        if ((pos + 1) != parent.adapter?.itemCount) {
            outRect.bottom = lineHeight.toInt()
        }
    }

    override fun onDraw(
        c: Canvas,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val pos = parent.getChildAdapterPosition(child)
            if (pos < 0) return
            if ((pos + 1) == parent.adapter?.itemCount) return
            val left = leftMargin
            val right = parent.width - rightMargin
            val top = child.bottom.toFloat()
            val bottom = top + lineHeight
            c.drawRect(left, top, right, bottom, paint)
        }
    }
}
