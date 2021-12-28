package com.gfq.baservadapter.simple


/**
class MainActivity : BaseActivity<ActivityMainBinding>() {

    override fun layout(): Int = R.layout.activity_main

    override fun initView() {

        val adapter = object : BaseRVAdapter<TestBean>(R.layout.select_item) {
            override fun onBindView(holder: BaseVH, data: TestBean, position: Int) {
                holder.get<SelectItemBinding>().run {
                    textView.text = data.name
                    tvDelete.setOnClickListener {
                        doSingleSelect(holder,position)
                    }
                }
            }

            override fun onItemSelected(holder: BaseVH, data: TestBean, position: Int) {
                holder.get<SelectItemBinding>().run {
                    textView.setTextColor(Color.RED)
                }
            }

            override fun onItemNotSelect(holder: BaseVH, data: TestBean, position: Int) {
                holder.get<SelectItemBinding>().run {
                    textView.setTextColor(Color.GREEN)
                }
            }

            override fun onItemReSelect(holder: BaseVH, data: TestBean, position: Int) {
                holder.get<SelectItemBinding>().run {
                    textView.setTextColor(Color.WHITE)
                }
            }
        }
        RefreshHelper(
            context = this,
            smartRefreshLayout = binding.smartRefreshLayout,
            recyclerView = binding.recyclerView,
            adapter = adapter,
            requestData = { curPage, pageDataNumber, callback ->
                callback(getDataList(curPage, pageDataNumber))
            },
            onRefreshStateChange = {
                Log.e("xxxx", "onRefreshStateChange " + it.name)
                false
            }
        )
    }

    private fun getDataList(curPage: Int, pageDataNumber: Int): List<TestBean>? {
        val list = mutableListOf<TestBean>()
        repeat(pageDataNumber) {
            list.add(TestBean("aaa curPage = $curPage ; i = $it"))
        }
//        if(curPage==2){
//            list.clear()
//        }
        if (curPage == 1) {
            return null
        }
        return list
    }
}*/
