# BaseRvAdapter

` implementation 'com.github.GaoFuq:BaseRvAdapter:release' `

使用 databinding 封装 recyclerview 的 Adapter。
封装了 单选 和 多选 功能。
处理了item删除后，position出错的问题。

v1.4.0 
封装了分页，刷新，加载更多，缓存 RefreshHelper
用到了第三方:
```kotlin
implementation 'com.scwang.smart:refresh-layout-kernel:2.0.3'
implementation 'com.scwang.smart:refresh-footer-classics:2.0.3' //经典加载
implementation 'com.scwang.smart:refresh-header-classics:2.0.3' //经典刷新头
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
