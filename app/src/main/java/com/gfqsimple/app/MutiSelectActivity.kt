package com.gfqsimple.app

import android.graphics.Color
import android.util.Log
import androidx.recyclerview.widget.SimpleItemAnimator
import com.gfq.baservadapter.BaseRVAdapter
import com.gfq.baservadapter.BaseVH
import com.gfq.baservadapter.get
import com.gfqsimple.app.databinding.SelectActivityBinding
import com.gfqsimple.app.databinding.SelectItemBinding

/**
 *  2021/12/23 11:25
 * @auth gaofuq
 * @description
 */


class MutiSelectActivity : BaseActivity<SelectActivityBinding>() {
    override fun layout(): Int = R.layout.select_activity

    override fun initView() {
        val adapter = object : BaseRVAdapter<TestBean>(R.layout.select_item) {
            override fun onBindView(holder: BaseVH, data: TestBean, position: Int) {
                holder.get<SelectItemBinding>().run {
                    textView.text = data.name
                    textView.setOnClickListener {
                        doMultipleSelect(
                            holder,
                            position,
                            4,
                            onCountOverMax = { Log.e("xx", "onCountOverMax") },
                        )
                    }
                    tvDelete.setOnClickListener {
                        removeAt(position)
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
        }
        adapter.dataList = mutableListOf<TestBean>().apply {
            add(TestBean("aaaa"))
            add(TestBean("bbb"))
            add(TestBean("ccc"))
            add(TestBean("ddd"))
            add(TestBean("fff"))
            add(TestBean("ggg"))
        }
        binding.recyclerView.adapter = adapter
        val amt = binding.recyclerView.itemAnimator
        if (amt is SimpleItemAnimator) {
            amt.supportsChangeAnimations = false
        }

    }
}