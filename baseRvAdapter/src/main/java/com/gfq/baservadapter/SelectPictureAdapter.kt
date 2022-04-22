package com.gfq.baservadapter

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.view.View
import androidx.fragment.app.Fragment
import com.gfq.baservadapter.adapter.BaseRVAdapter
import com.gfq.baservadapter.adapter.BaseVH
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia

/**
 *  2022/1/24 10:07
 * @auth gaofuq
 */


/**
 *  * 添加和删除图片 Adapter。
 *  * 默认实现了 选择图片，删除图片，设置图片。
 *  * 图片数量 [maxSelectNum] 默认9。
 *  * 触发删除图片的view需要设置[deleteTag]
 *  * 选择图片
 * @see [selectPictureWithCrop]
 * @see [selectPictureWithSquareCrop]
 * @see [selectPictureWithCircleCrop]
 * @see [selectPictureWithCompress]
 */
abstract class SelectPictureAdapter(
    itemLayoutRes: Int,
    /** * 删除图片的标记*/
    private val deleteTag: String,
    private val maxSelectNum: Int = 9,
) :
    BaseRVAdapter<String>(itemLayoutRes) {



    /**
     * * 添加图片按钮itemView绑定
     */
    abstract fun onBindViewAdd(holder: BaseVH, data: String, position: Int)
    /**
     * * 其他itemView绑定
     */
    abstract fun onBindViewOther(holder: BaseVH, data: String, position: Int)



    /**
     * * 重新选择图片
     * @see [selectPictureWithCrop]
     * @see [selectPictureWithSquareCrop]
     * @see [selectPictureWithCircleCrop]
     * @see [selectPictureWithCompress]
     */
    fun onReSelectPictureClick(position: Int) {
        reSelectPicturePosition = position
    }

    private var reSelectPicturePosition: Int = -1

    lateinit var pictureSelector: PictureSelector
        private set

    /**
     * * 剩余可选图片数量
     */
    var selectRemainNum: Int = maxSelectNum

    /**
     * * 选择图片的标记
     */
    val addTag: String = "addPicture"

    override var dataList = mutableListOf<String>()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            if (maxSelectNum > 1) {
                if (value.size < maxSelectNum) {
                    value.remove(addTag)
                    value.add(addTag)
                }
            }
            field = value
            notifyDataSetChanged()
        }

    enum class Type {
        crop, compress, none
    }

    private val map = mutableMapOf<Int, Type>()
    private val requestCodeCrop = 111_111
    private val requestCodeSquareCrop = 111_112
    private val requestCodeCircleCrop = 111_113
    private val requestCodeCompress = 111_114

    fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        block: (picturePathList: List<String>) -> Unit = {},
    ) {
        if (resultCode == RESULT_OK) {
            val list = PictureSelector.obtainMultipleResult(data)
            if (list.isNullOrEmpty()) return
            val tempList = mutableListOf<String>()
            when (map[requestCode] ?: Type.none) {
                Type.crop -> {
                    list.forEach {
                        if (it.cutPath == null) {
                            tempList.add(it.realPath)
                        } else {
                            tempList.add(it.cutPath)
                        }
                    }
                }
                Type.compress -> {
                    list.forEach {
                        if (it.compressPath == null) {
                            tempList.add(it.realPath)
                        } else {
                            tempList.add(it.compressPath)
                        }
                    }
                }
                Type.none -> {
                    list.map { it.realPath }
                }
            }

            if (reSelectPicturePosition >= 0) {
                //重选
                if (tempList.isNotEmpty()) {
                    updateItem(reSelectPicturePosition, tempList[0])
                }
                reSelectPicturePosition = -1
            } else {
                //正常添加
                if (selectRemainNum > 0) {
                    selectRemainNum -= tempList.size
                }
                remove(addTag)
                addAll(tempList)

                block(dataList)

                if (dataList.size < maxSelectNum && selectRemainNum > 0) {
                    add(addTag)
                }
            }


        }
    }

    override fun onBindView(holder: BaseVH, data: String, position: Int) {
        if (data == addTag) {
            //选择图片
            onBindViewAdd(holder, data, position)
        } else {
            //删除图片
            holder.itemView.findViewWithTag<View>(deleteTag)?.setOnClickListener {
                selectRemainNum++
                if (selectRemainNum > maxSelectNum) {
                    selectRemainNum = maxSelectNum
                }
                remove(addTag)
                removeAt(position)
                if (dataList.size < maxSelectNum) {
                    add(addTag)
                }
            }
            onBindViewOther(holder, data, position)
        }



    }


    /**
     * 图片预览
     */
    fun preview(position: Int, localMedias: List<LocalMedia>) {
        if (::pictureSelector.isInitialized) {
            pictureSelector
                .themeStyle(R.style.picture_default_style)
                .isNotPreviewDownload(true)
                .imageEngine(GlideEngine.createGlideEngine())
                .openExternalPreview(position, localMedias)
        }
    }

    /**
     * 图片预览
     */
    @JvmName("preview1")
    fun preview(position: Int, pathList: List<String>) {
        if (::pictureSelector.isInitialized) {
            val mediaList = mutableListOf<LocalMedia>()
            pathList.forEach {
                if (it != addTag) {
                    val media = LocalMedia()
                    media.path = it
                    mediaList.add(media)
                }
            }
            pictureSelector
                .themeStyle(R.style.picture_default_style)
                .isNotPreviewDownload(true)
                .imageEngine(GlideEngine.createGlideEngine())
                .openExternalPreview(position, mediaList)
        }
    }


    /**
     * 选择图片，方形裁剪
     */
    fun selectPictureWithSquareCrop() {
        selectPictureWithCrop(requestCode = requestCodeSquareCrop)
    }

    /**
     * 选择图片，圆形裁剪
     */
    fun selectPictureWithCircleCrop() {
        selectPictureWithCrop(isCircleCrop = true, requestCode = requestCodeCircleCrop)
    }

    /**
     * 选择图片，裁剪
     */
    fun selectPictureWithCrop(
        isCircleCrop: Boolean = false,
        aspect_ratio_x: Int = 1,
        aspect_ratio_y: Int = 1,
        requestCode: Int = requestCodeCrop,
    ) {
        if (!::pictureSelector.isInitialized) return
        map[requestCode] = Type.crop
        pictureSelector.openGallery(PictureMimeType.ofImage())
            .isWeChatStyle(true)
            .imageEngine(GlideEngine.createGlideEngine())
            .isNotPreviewDownload(true)
            .maxSelectNum(selectRemainNum)
            .isEnableCrop(true)
            .isCompress(false)
            .circleDimmedLayer(isCircleCrop)//是否圆形裁剪
            .withAspectRatio(aspect_ratio_x, aspect_ratio_y)// 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
            .freeStyleCropEnabled(false)// 裁剪框是否可拖拽
            .showCropFrame(false)// 是否显示裁剪矩形边框 圆形裁剪时建议设为false
            .showCropGrid(false)// 是否显示裁剪矩形网格 圆形裁剪时建议设为false
            .rotateEnabled(false)
            .forResult(requestCode)
    }

    /**
     * 选择图片，压缩
     */
    fun selectPictureWithCompress() {
        if (!::pictureSelector.isInitialized) return
        map[requestCodeCompress] = Type.compress
        pictureSelector.openGallery(PictureMimeType.ofImage())
            .isWeChatStyle(true)
            .imageEngine(GlideEngine.createGlideEngine())
            .isNotPreviewDownload(true)
            .maxSelectNum(selectRemainNum)
            .isEnableCrop(false)
            .isCompress(true)
            .circleDimmedLayer(false)//是否圆形裁剪
            .freeStyleCropEnabled(false)// 裁剪框是否可拖拽
            .showCropFrame(false)// 是否显示裁剪矩形边框 圆形裁剪时建议设为false
            .showCropGrid(false)// 是否显示裁剪矩形网格 圆形裁剪时建议设为false
            .rotateEnabled(false)
            .forResult(requestCodeCompress)
    }

    fun initPictureSelector(activity: Activity): SelectPictureAdapter {
        if (!::pictureSelector.isInitialized) {
            pictureSelector = PictureSelector.create(activity)
        }
        return this
    }

    fun initPictureSelector(fragment: Fragment): SelectPictureAdapter {
        if (!::pictureSelector.isInitialized) {
            pictureSelector = PictureSelector.create(fragment)
        }
        return this
    }


}