package com.gfq.baservadapter.refresh

import android.view.LayoutInflater
import android.view.View

enum class State{
        NONE ,
        REFRESH_START ,
        REFRESH_SUCCESS,

        LOAD_MORE_START,
        LOAD_MORE_SUCCESS,

        ERROR,
        /**
         * 列表没有数据
         */
        EMPTY_DATA,
        /**
         * 当前加载更多请求返回的数据为null或empty 或者 数据量小于dataPerPage
         */
        LOAD_MORE_NO_MORE_DATA,

        /**
         * 当前加载更多请求返回的数据为null或empty 或者 数据量小于dataPerPage
         */
        REFRESH_NO_MORE_DATA,


        /**
         * 当前刷新请求返回的数据为null或empty
         */
        EMPTY_DATA_ON_REFRESH,


        NET_LOSE,
    }
