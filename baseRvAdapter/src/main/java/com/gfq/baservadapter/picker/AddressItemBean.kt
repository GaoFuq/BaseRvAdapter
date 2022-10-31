package com.gfq.baservadapter.picker

import com.gfq.baservadapter.refresh.RVSelect

data class AddressItemBean(
    val code: String? = null,
    val name: String? = null,
    override var select: Boolean = false,
): RVSelect
