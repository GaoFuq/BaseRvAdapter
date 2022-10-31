package com.gfq.baservadapter.picker

import android.graphics.Color
import com.gfq.baservadapter.R
import com.gfq.baservadapter.adapter.BaseRVSelectAdapter
import com.gfq.baservadapter.adapter.BaseVH
import com.gfq.baservadapter.databinding.ItemAddressPickerBinding
import com.gfq.baservadapter.refresh.get

open class AddressPickerAdapter : BaseRVSelectAdapter<AddressItemBean>(R.layout.item_address_picker) {
    var onItemSelect: ((AddressItemBean) -> Unit)? = null
    override fun onBindView(holder: BaseVH, data: AddressItemBean, position: Int) {}

    override fun onItemSelected(holder: BaseVH?, data: AddressItemBean, position: Int) {
        onItemSelect?.invoke(data)
        holder?.get<ItemAddressPickerBinding>()?.run {
            tvAddressItem.text = data.name
            tvAddressItem.setTextColor(Color.parseColor("#333333"))
        }
    }

    override fun onItemNotSelect(holder: BaseVH?, data: AddressItemBean, position: Int) {
        holder?.get<ItemAddressPickerBinding>()?.run {
            tvAddressItem.text = data.name
            tvAddressItem.setTextColor(Color.parseColor("#999999"))
        }
    }
}
