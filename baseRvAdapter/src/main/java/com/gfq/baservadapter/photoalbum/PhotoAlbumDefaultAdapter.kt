package com.gfq.baservadapter.photoalbum

import android.content.Intent
import com.luck.picture.lib.PictureSelector

/**
 *  2022/5/13 9:50
 * @auth gaofuq
 * @description
 */

/**
 * * 实现 [PhotoAlbumBaseAdapter.onBindAdder]
 * * 实现 [PhotoAlbumBaseAdapter.onBind]
 * * 在init{}中初始化配置
init {
isShowGif = true
maxFileSize = 10
}
 * * 打开相册 [PhotoAlbum.open]
 * * 最后在 onActivityResult 中调用 [PhotoAlbumBaseAdapter.onActivityResult]
 */
abstract class PhotoAlbumDefaultAdapter(
    activityOrFragment: Any,
    itemLayoutRes: Int,
    private val isShowGif: Boolean = false,
    //文件的大小,单位 M
    val maxFileSize: Float = 10f,
    maxSelectNum: Int = 9,
    order: Order = Order.inOrder,
) : PhotoAlbumBaseAdapter(itemLayoutRes, maxSelectNum, order) {


    var pictureSelector: PictureSelector = createPictureSelector(activityOrFragment)
        private set


    init {
        add("")
    }


    override fun open() {
        pictureSelector.openWithCompress(
            maxSelectNum = maxSelectNum,
            selectedNum = dataList.size-1,
            isShowGif = isShowGif
        )
    }

    override fun getResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
    ): List<String> {
        return pictureSelector.getResult(requestCode, resultCode, data)
    }
}