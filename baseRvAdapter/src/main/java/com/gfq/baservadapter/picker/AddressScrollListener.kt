package com.gfq.baservadapter.picker

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.gfq.baservadapter.adapter.BaseRVSelectAdapter

class AddressScrollListener(private val snapHelper: PagerSnapHelper, private val adapter: BaseRVSelectAdapter<*>):
    RecyclerView.OnScrollListener(){
    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val snapView = snapHelper.findSnapView(layoutManager)
            if (snapView != null) {
                val position = recyclerView.getChildAdapterPosition(snapView)
                adapter.doSingleSelect(null,position)
            }
        }
    }
}