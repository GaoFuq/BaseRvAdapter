package com.gfqsimple.app

import android.graphics.Color
import android.util.Log
import com.gfq.baservadapter.*
import com.gfqsimple.app.databinding.SelectActivityBinding
import com.gfqsimple.app.databinding.SelectItemBinding

/**
 *  2021/12/23 11:25
 * @auth gaofuq
 * @description
 */

class SingleSelectActivity : BaseActivity<SelectActivityBinding>() {
    override fun layout(): Int = R.layout.select_activity

    override fun initView() {
        val adapter = object : BaseRVAdapter<TestBean>(R.layout.select_item) {
            override fun onBindView(holder: BaseVH, data: TestBean, position: Int) {
                holder.get<SelectItemBinding>().run {
                    textView.text = data.name
                    textView.setOnClickListener {
                        doSingleSelect(holder,position)
                    }
                    tvDelete.setOnClickListener {
                        removeAt(holder.bindingAdapterPosition)
                        Log.e("xx", "onReSelectListener adapter dataList = $dataList")
                    }
                }
            }

            override fun onItemSelected(holder: BaseVH, data: TestBean, position: Int) {
                holder.get<SelectItemBinding>().textView.setTextColor(Color.RED)
            }

            override fun onItemNotSelect(holder: BaseVH, data: TestBean, position: Int) {
                holder.get<SelectItemBinding>().textView.setTextColor(Color.BLACK)
            }

            override fun onItemReSelect(holder: BaseVH, data: TestBean, position: Int) {
                holder.get<SelectItemBinding>().textView.text = "reSelect"
            }
        }
        val list = mutableListOf<TestBean>()
        repeat(50){
            list .add(TestBean("aaaa $it"))
        }
        adapter.dataList =list
        adapter.recyclerView = binding.recyclerView
//        binding.recyclerView.adapter = adapter



    }
}