package com.gfq.baservadapter.refresh

import android.view.LayoutInflater
import android.view.View

enum class State{
        NONE ,
        LOADING ,

        REFRESH_SUCCESS,
        LOAD_MORE_SUCCESS,

        ERROR,
        /**
         * 列表没有数据
         */
        EMPTY_DATA,
        /**
         * 当前加载更多请求返回的数据为null或empty
         */
        NO_MORE_DATA_LOADMORE,

        /**
         * 当前刷新请求返回的数据为null或empty
         */
        NO_MORE_DATA_REFRESH,


        NET_LOSE,
    }
