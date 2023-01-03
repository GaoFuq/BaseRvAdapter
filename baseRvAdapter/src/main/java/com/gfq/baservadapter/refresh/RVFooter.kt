package com.gfq.baservadapter.refresh

import com.gfq.baservadapter.R

open class RVFooter(
    val isFooter: Boolean = false,
    override var viewType: Int? = R.id.refresh_body_layout,
) : RVType
