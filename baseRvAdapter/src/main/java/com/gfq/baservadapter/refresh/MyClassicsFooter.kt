package com.gfq.baservadapter.refresh

import android.content.Context
import android.widget.ImageView
import android.widget.TextView
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.layout.constant.SpinnerStyle

class MyClassicsFooter(context: Context?) : ClassicsFooter(context) {

     var noMoreDataIconIdRes: Int = 0
     var noMoreDataText: String = "我是有底线的~"

    override fun getSpinnerStyle(): SpinnerStyle {
        return SpinnerStyle.Translate
    }

    override fun setNoMoreData(noMoreData: Boolean): Boolean {
        if (mNoMoreData != noMoreData) {
            mNoMoreData = noMoreData
            val arrowView: ImageView = mArrowView
            if (noMoreData) {
                mTitleText.text = noMoreDataText
                arrowView.rotation = 0f
                setArrowResource(noMoreDataIconIdRes)
                arrowView.setImageResource(noMoreDataIconIdRes)
            } else {
                mTitleText.text = mTextPulling
            }
        }
        return true
    }


    fun setNoMoreDataTextStyle(style: TextView.() -> Unit) {
        style(mTitleText)
    }

    fun setIconStyle(style: ImageView.() -> Unit) {
        style(mArrowView)
    }
}