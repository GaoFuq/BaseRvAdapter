package com.gfq.baservadapter

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding

/**
 *  2021/12/6 11:16
 * @auth gaofuq
 * @description
 */

class Test :AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createRefreshHelper<String>(
            itemLayoutId = R.layout.select_item,
            refreshContainerView = null,
            onAdapterBindView ={holder: BaseVH, data: String, position: Int ->

            },
            request = {curPage: Int, pageCount: Int, callback: (List<String>?) -> Unit ->

            },
            onRefreshStateChange = {
                false
            }
        ) .also {
//            setContentView(it.smartRefreshLayout)
//            it.smartRefreshLayout.
//            it.recyclerView.
//            it.adapter.
//            it.
        }
    }
}

