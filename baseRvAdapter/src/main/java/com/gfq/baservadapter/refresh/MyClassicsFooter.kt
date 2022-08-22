package com.gfq.baservadapter.refresh

import android.content.Context
import android.widget.ImageView
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.layout.constant.SpinnerStyle

class MyClassicsFooter(
    context: Context?,
    private val noMoreDataIconIdRes: Int = 0,
    private val noMoreDataText: String = "我是底线的~",
) : ClassicsFooter(context) {
    override fun getSpinnerStyle(): SpinnerStyle {
        return SpinnerStyle.Translate
    }

    override fun setNoMoreData(noMoreData: Boolean): Boolean {
        if (mNoMoreData != noMoreData) {
            mNoMoreData = noMoreData
            val arrowView: ImageView = mArrowView
            if (noMoreData) {
                mTitleText.text = noMoreDataText
                arrowView.setImageResource(noMoreDataIconIdRes)
            } else {
                mTitleText.text = mTextPulling
            }
        }
        return true
    }

}