package com.gfq.baservadapter.refresh

/**
 *  每一个 state 都会回调，可能有连续相同的 state 。
 *  监听每一个 state 。
 */
interface OnNextStateListener<T> {
    fun onNext(helper: RefreshHelper<T>, state: State)
}