package com.gfq.baservadapter.photoalbum

import android.content.Intent

/**
 *  2022/5/13 9:46
 * @auth gaofuq
 * @description
 */
interface PhotoAlbum {
    /**
     * 打开相册，并进入选择照片的界面
     */
    fun open()

    /**
     * 获取选择的图片或视频路径
     */
    fun getResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ): List<String>
}