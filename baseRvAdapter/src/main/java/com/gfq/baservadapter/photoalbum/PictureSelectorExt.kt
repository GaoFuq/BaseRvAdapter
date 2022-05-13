package com.gfq.baservadapter.photoalbum

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment
import com.gfq.baservadapter.GlideEngine
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import java.lang.RuntimeException

/**
 *  2022/5/13 14:07
 * @auth gaofuq
 * @description
 */

/**
 * 选择图片后的处理方式
 */
private enum class HandleResultType {
    /**
     * 裁剪
     */
    crop,

    /**
     * 压缩
     */
    compress,

    /**
     * 不处理
     */
    none
}

enum class RequestCode(val code: Int) {
    /**
     * 自动处理数据。在[Activity.onActivityResult]中调用[getResult]获取结果。
     */
    auto(6660),

    /**
     * 自定义数据结果的处理。
     */
    custom(7770)

}

private var currentType = HandleResultType.none


/**
 * 选择图片，方形裁剪。
 * 不支持Gif裁剪。
 * 裁剪后的图片不需要再压缩。
 */

fun PictureSelector.openWithSquareCrop(
    maxSelectNum: Int = 9,
    selectedNum: Int,
    maxFileSize: Float = 10f,
) {
    openWithCrop(
        requestCode = RequestCode.auto,
        maxSelectNum = maxSelectNum,
        selectedNum = selectedNum,
        maxFileSize = maxFileSize,
        aspect_ratio_x = 1,
        aspect_ratio_y = 1,
        isCircleCrop = false)
}

/**
 * 选择图片，圆形裁剪。
 * 不支持Gif裁剪。
 * 裁剪后的图片不需要再压缩。
 */

fun PictureSelector.openWithCircleCrop(
    maxSelectNum: Int = 9,
    selectedNum: Int,
    maxFileSize: Float = 10f,
) {
    openWithCrop(
        requestCode = RequestCode.auto,
        maxSelectNum = maxSelectNum,
        selectedNum = selectedNum,
        maxFileSize = maxFileSize,
        aspect_ratio_x = 1,
        aspect_ratio_y = 1,
        isCircleCrop = true)
}

/**
 * 选择图片,按比例裁剪。
 * 不支持Gif裁剪。
 * 裁剪后的图片不需要再压缩。
 */

fun PictureSelector.openWithCrop(
    requestCode: RequestCode = RequestCode.auto,
    maxSelectNum: Int,
    selectedNum: Int,
    maxFileSize: Float,
    aspect_ratio_x: Int,
    aspect_ratio_y: Int,
    isCircleCrop: Boolean,
) {
    currentType = HandleResultType.crop
    openGallery(PictureMimeType.ofImage())
        .isWeChatStyle(true)
        .imageEngine(GlideEngine.createGlideEngine())
        .isNotPreviewDownload(true)
        .maxSelectNum(maxSelectNum - selectedNum)
        .isEnableCrop(true)
        .isCompress(false)
        .isGif(false)
        .queryMaxFileSize(maxFileSize)
        .circleDimmedLayer(isCircleCrop)//是否圆形裁剪
        .withAspectRatio(aspect_ratio_x, aspect_ratio_y)// 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
        .freeStyleCropEnabled(false)// 裁剪框是否可拖拽
        .showCropFrame(false)// 是否显示裁剪矩形边框 圆形裁剪时建议设为false
        .showCropGrid(false)// 是否显示裁剪矩形网格 圆形裁剪时建议设为false
        .rotateEnabled(false)
        .forResult(requestCode.code)
}

/**
 * 选择图片，压缩。
 * 支持同时选择图片和Gif，但是选择的Gif不会被压缩。
 */

fun PictureSelector.openWithCompress(
    requestCode: RequestCode = RequestCode.auto,
    maxSelectNum: Int = 9,
    selectedNum: Int,
    maxFileSize: Float = 10f,
    isShowGif: Boolean = false,
) {
    currentType = HandleResultType.compress
    openGallery(PictureMimeType.ofImage())
        .isWeChatStyle(true)
        .imageEngine(GlideEngine.createGlideEngine())
        .isNotPreviewDownload(true)
        .maxSelectNum(maxSelectNum - selectedNum)
        .isEnableCrop(false)
        .isCompress(true)
        .isGif(isShowGif)
        .queryMaxFileSize(maxFileSize)
        .circleDimmedLayer(false)//是否圆形裁剪
        .freeStyleCropEnabled(false)// 裁剪框是否可拖拽
        .showCropFrame(false)// 是否显示裁剪矩形边框 圆形裁剪时建议设为false
        .showCropGrid(false)// 是否显示裁剪矩形网格 圆形裁剪时建议设为false
        .rotateEnabled(false)
        .forResult(requestCode.code)
}


/**
 * 获取选择的图片的结果集。
 * @param requestCode 必须是 [RequestCode.auto]
 */
fun PictureSelector.getResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?,
): List<String> {
    if (resultCode != Activity.RESULT_OK) return emptyList()
    if (requestCode != RequestCode.auto.code) return emptyList()
    val list = PictureSelector.obtainMultipleResult(data)
    if (list.isNullOrEmpty()) return emptyList()
    val tempList = arrayListOf<String>()
    when (currentType) {
        HandleResultType.crop -> {
            list.forEach {
                tempList.add(it.cutPath ?: it.path)
            }
        }

        HandleResultType.compress -> {
            list.forEach {
                tempList.add(it.compressPath ?: it.path)
            }
        }

        HandleResultType.none -> {
            tempList.addAll(list.map { it.realPath })
        }
    }
    return tempList
}


fun createPictureSelector(activityOrFragment: Any): PictureSelector {
    return when (activityOrFragment) {
        is Activity -> {
            PictureSelector.create(activityOrFragment)
        }
        is Fragment -> {
            PictureSelector.create(activityOrFragment)
        }
        else -> {
            throw RuntimeException("PhotoAlbumDefaultAdapter : param [activityOrFragment] is not Activity or Fragment")
        }
    }
}

@JvmName("previewString")
fun PictureSelector.preview(position: Int, pathList: List<String>) {
    val mediaList = mutableListOf<LocalMedia>()
    pathList.forEach {
        if (it.isNotEmpty()) {
            val media = LocalMedia()
            media.path = it
            mediaList.add(media)
        }
    }
    preview(position, mediaList)
}

@JvmName("previewMedia")
fun PictureSelector.preview(position: Int, mediaList: List<LocalMedia>) {
    themeStyle(com.gfq.baservadapter.R.style.picture_default_style)
        .isNotPreviewDownload(true)
        .imageEngine(GlideEngine.createGlideEngine())
        .openExternalPreview(position, mediaList)
}

