# BaseRvAdapter
使用 databinding 封装 recyclerview 的 Adapter。
封装了 单选 和 多选 功能。
处理了item删除后，position出错的问题。

        ......
        
        val adapter = object : BaseRVAdapter<TestBean>(R.layout.select_item) {
            override fun onBindView(holder: BaseVH, data: TestBean, position: Int) {
                holder.get<SelectItemBinding>().run {
                    textView.text = data.name
                    textView.setOnClickListener {
                        /* doSingleSelect(holder,position) */
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
            
             override fun onItemReSelect(holder: BaseVH, data: TestBean, position: Int) {
                holder.get<SelectItemBinding>().textView.text = "reSelect"
            }
        }
        
        adapter.recyclerView = binding.recyclerView
        
        ......

