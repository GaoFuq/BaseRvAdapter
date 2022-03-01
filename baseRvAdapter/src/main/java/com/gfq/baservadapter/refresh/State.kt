package com.gfq.baservadapter.refresh

import android.view.LayoutInflater
import android.view.View

enum class State{
        LOADING ,

        ERROR,
        /**
         * 列表没有数据
         */
        EMPTY_DATA,
        /**
         * 当前加载更多请求返回的数据为null或empty
         */
        EMPTY_DATA_WITH_LOAD_MORE,

        NET_LOSE,
    }
