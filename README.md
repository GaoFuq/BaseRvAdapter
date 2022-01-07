# BaseRvAdapter

` implementation 'com.github.GaoFuq:BaseRvAdapter:1.4.0' `

使用 databinding 封装 recyclerview 的 Adapter。
封装了 单选 和 多选 功能。
处理了item删除后，position出错的问题。

v1.4.0 
封装了分页，刷新，加载更多 RefreshHelper
用到了第三方。
```kotlin
implementation 'com.scwang.smart:refresh-layout-kernel:2.0.3'
implementation 'com.scwang.smart:refresh-footer-classics:2.0.3' //经典加载
implementation 'com.scwang.smart:refresh-header-classics:2.0.3' //经典刷新头
```


1.使用 RefreshHelper ：
```kotlin

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
}

```



2.单独使用BaseRVAdapter ：

```kotlin
  
        
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
        
     
 ```

 3. 依赖：
 ```
    implementation 'androidx.core:core-ktx:1.5.0'
    implementation 'androidx.appcompat:appcompat:1.3.0'
    implementation 'com.google.android.material:material:1.4.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.2'
    //JetPack
    implementation "androidx.lifecycle:lifecycle-extensions:2.2.0"
    implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.1"
    implementation "androidx.lifecycle:lifecycle-livedata-ktx:2.3.1"

    //协程
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3"
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.4.3"
    implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.1"
    implementation "androidx.lifecycle:lifecycle-runtime-ktx:2.3.1"
    implementation "androidx.lifecycle:lifecycle-livedata-ktx:2.3.1"

    implementation 'com.github.GaoFuq:BaseRvAdapter:1.4.0'
    implementation  'com.scwang.smart:refresh-layout-kernel:2.0.3'
    implementation  'com.scwang.smart:refresh-footer-classics:2.0.3'    //经典加载
    implementation  'com.scwang.smart:refresh-header-classics:2.0.3'    //经典刷新头

    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    implementation 'com.google.code.gson:gson:2.8.6'
    implementation 'com.squareup.okhttp3:okhttp:4.8.0'
    implementation "com.squareup.retrofit2:adapter-rxjava3:2.9.0"
    implementation 'com.squareup.okhttp3:logging-interceptor:4.9.0'
 ```
