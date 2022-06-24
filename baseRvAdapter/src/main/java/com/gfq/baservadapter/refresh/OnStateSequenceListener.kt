package com.gfq.baservadapter.refresh

/**
 *  监听每一个 state 。
 *  每一个 state 都会回调，可能有连续相同的 state 。
 */
interface OnStateSequenceListener<T> {
    fun onNext(helper: RefreshHelper<T>, state: State)
}