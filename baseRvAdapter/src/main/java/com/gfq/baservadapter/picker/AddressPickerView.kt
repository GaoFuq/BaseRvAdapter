package com.gfq.baservadapter.picker

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.PagerSnapHelper
import com.gfq.baservadapter.R
import com.gfq.baservadapter.databinding.AddressPickerViewBinding

/**
 *  2022/9/6 9:49
 * @auth gaofuq
 * @description 自定义的地址选择 底部弹窗，使用接口获取数据
 */


class AddressPickerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs) {

    var province: String = ""
    var city: String = ""
    var district: String = ""

    private var tempSize = 0

    var visibleItemCount = 5 //奇数，最小3
        set(value) {
            field = value
            val tempBean = AddressItemBean()
            if (value < 3) {
                field = 3
            }
            headerList.clear()
            footerList.clear()
            tempSize = (field - 1) / 2
            repeat(tempSize) {
                headerList.add(tempBean)
                footerList.add(tempBean)
            }
        }


    private val headerList = mutableListOf<AddressItemBean>()
    private val footerList = mutableListOf<AddressItemBean>()

    private val adapterProvince = AddressPickerAdapter()
    private val adapterCity = AddressPickerAdapter()
    private val adapterDistrict = AddressPickerAdapter()

    private val snapHelperProvince = PagerSnapHelper()
    private val snapHelperCity = PagerSnapHelper()
    private val snapHelperDistrict = PagerSnapHelper()

    val vBinding by lazy {
        DataBindingUtil.inflate<AddressPickerViewBinding>(
            LayoutInflater.from(context),
            R.layout.address_picker_view,
            this,
            true
        )
    }

    /**
    requestAddressData = {
    val resp = withContext(Dispatchers.IO){ api.getAddressData(it) }
    if (resp?.code == ErrorCode.SUCCESS.code) {
    return resp.data?.map { AddressItemBean(it.code, it.name) }?.toMutableList()
    ?: mutableListOf()
    } else {
    toast(resp?.message ?: "请求失败")
    return mutableListOf()
    }
    }
     */
    var requestAddressData: ((code: String?) -> MutableList<AddressItemBean>)? = null

    fun getAddressText():String = "$province $city $district"

    init {
        initView()
    }



    private fun initView() {
        vBinding.rvProvince.run {
            adapter = adapterProvince
            snapHelperProvince.attachToRecyclerView(this)
            addOnScrollListener(AddressScrollListener(snapHelperProvince, adapterProvince))
            adapterProvince.onItemSelect = {
                province = it.name ?: ""
                onProvinceSelected()
            }
        }

        vBinding.rvCity.run {
            adapter = adapterCity
            snapHelperCity.attachToRecyclerView(this)
            addOnScrollListener(AddressScrollListener(snapHelperCity, adapterCity))
            adapterCity.onItemSelect = {
                city = it.name ?: ""
                onCitySelected()
            }
        }

        vBinding.rvDistrict.run {
            adapter = adapterDistrict
            snapHelperDistrict.attachToRecyclerView(this)
            addOnScrollListener(AddressScrollListener(snapHelperDistrict, adapterDistrict))
            adapterDistrict.onItemSelect = {
                district = it.name ?: ""
            }
        }
    }

    // 初始化省数据，并选中该数据，位置居中。
    // 市 和 区 的数据，默认选中第一个，位置居中。
    // initProvinceCode 初始化省数据的 code。
    fun initProvinceData(initProvinceCode: String?) {
        if (initProvinceCode == null) {
            province = ""
            city = ""
            district = ""
            return
        }
        val provinceDataList = requestAddressData?.invoke(initProvinceCode)
        provinceDataList?.let {
            val position = it.indexOf(it.firstOrNull { a->a.code==initProvinceCode })
            val positionOffset = position - tempSize
            adapterProvince.dataList = (headerList + it + footerList).toMutableList()
            vBinding.rvProvince.scrollToPosition(positionOffset + tempSize)
            adapterProvince.doSingleSelect(null, positionOffset + tempSize * 2)
        }
    }


    private fun initCityData() {
        val selectProvince = adapterProvince.getSingleSelectData()
        if (selectProvince?.code == null) {
            city = ""
            district = ""
            return
        }
        val cityDataListTemp = requestAddressData?.invoke(selectProvince.code)
        cityDataListTemp?.let {
            val cityDataList = (headerList + it + footerList).toMutableList()
            adapterCity.dataList = cityDataList
            vBinding.rvCity.scrollToPosition(0)
            adapterCity.doSingleSelect(null, tempSize)
        }
    }

    private fun initDistrictData() {
        val selectCity = adapterCity.getSingleSelectData()
        if (selectCity?.code == null) {
            district = ""
            return
        }
        val cityDataListTemp = requestAddressData?.invoke(selectCity.code)
        cityDataListTemp?.let {
            val cityDataList = (headerList + it + footerList).toMutableList()
            adapterDistrict.dataList = cityDataList
            vBinding.rvDistrict.scrollToPosition(0)
            adapterDistrict.doSingleSelect(null, tempSize)
        }
    }


    private fun onCitySelected() {
        initDistrictData()
    }

    private fun onProvinceSelected() {
        initCityData()
        initDistrictData()
    }

}