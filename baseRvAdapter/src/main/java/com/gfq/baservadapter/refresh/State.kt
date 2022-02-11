package com.gfq.baservadapter.refresh

import android.view.LayoutInflater
import android.view.View

enum class State{
        IDLE,
        REFRESH_LOADING ,
        REFRESH_SUCCESS,
        REFRESH_ERROR,

        LOAD_MORE_LOADING,
        LOAD_MORE_SUCCESS,
        LOAD_MORE_ERROR,

        /**
         * 列表没有数据
         */
        EMPTY_DATA,
        /**
         * 当前加载更多请求返回的数据为null或empty
         */
        EMPTY_DATA_WITH_LOAD_MORE,

        /**
         * 当前刷新请求返回的数据为null或empty
         */
        EMPTY_DATA_WITH_REFRESH,
        NET_LOSE,
    }
