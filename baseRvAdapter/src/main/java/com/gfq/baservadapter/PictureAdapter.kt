package com.gfq.baservadapter

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
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
 *  * 需要设置 [deleteTag] ,[pictureTargetTag] 参数。
 */
abstract class PictureAdapter(
    itemLayoutRes: Int,
    /** * 删除图片的标记*/
    val deleteTag: String? = null,
    val pictureTargetTag: String? = null,
    val maxSelectNum: Int = 9,
) :
    BaseRVAdapter<String>(itemLayoutRes) {

    /**
     * * 选择图片操作
     * @see [selectPictureWithCrop]
     * @see [selectPictureWithSquareCrop]
     * @see [selectPictureWithCircleCrop]
     * @see [selectPictureWithCompress]
     */
    abstract fun onAddPictureClick()

    /**
     * * 绑定其他内容
     */
    abstract fun onBindViewOther(holder: BaseVH, data: String, position: Int)

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
                if (value.size < maxSelectNum && !value.contains(addTag)) {
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


    fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        block: (picturePathList: List<String>) -> Unit,
    ) {
        if (resultCode == RESULT_OK) {
            val list = PictureSelector.obtainMultipleResult(data)
            if (list.isNullOrEmpty()) return
            val useList: List<String> = when (map[requestCode] ?: Type.none) {
                Type.crop -> {
                    list.map { it.cutPath }
                }
                Type.compress -> {
                    list.map { it.compressPath }
                }
                Type.none -> {
                    list.map { it.realPath }
                }
            }

            block(useList)

            if (maxSelectNum > 1) {
                if (selectRemainNum > 0) {
                    selectRemainNum -= useList.size
                }
                remove(addTag)
                addAll(useList)
                if (dataList.size < maxSelectNum && selectRemainNum > 0) {
                    add(addTag)
                }
            }
        }
    }

    override fun onBindView(holder: BaseVH, data: String, position: Int) {
        if (data == addTag) {
            //选择图片
            holder.itemView.setOnClickListener { onAddPictureClick() }
        }
        //删除图片
        holder.itemView.findViewWithTag<View>(deleteTag)?.setOnClickListener {
            removeAt(position)
            selectRemainNum++
        }
        //设置图片到目标View
        holder.itemView.findViewWithTag<View>(pictureTargetTag)?.let {
            if (it is ImageView) {
                Glide.with(it).load(data).into(it)
            }
        }
        onBindViewOther(holder, data, position)
    }


    /**
     * 图片预览
     */
    fun preview(position: Int, localMedias: MutableList<LocalMedia>) {
        if (::pictureSelector.isInitialized) {
            pictureSelector
                .themeStyle(R.style.picture_default_style)
                .isNotPreviewDownload(true)
                .imageEngine(GlideEngine.createGlideEngine())
                .openExternalPreview(position, localMedias)
        }
    }


    /**
     * 选择图片，方形裁剪
     */
    fun selectPictureWithSquareCrop(requestCode: Int) {
        selectPictureWithCrop(requestCode = requestCode)
    }

    /**
     * 选择图片，圆形裁剪
     */
    fun selectPictureWithCircleCrop(requestCode: Int) {
        selectPictureWithCrop(isCircleCrop = true, requestCode = requestCode)
    }

    /**
     * 选择图片，裁剪
     */
    fun selectPictureWithCrop(
        isCircleCrop: Boolean = false,
        aspect_ratio_x: Int = 1,
        aspect_ratio_y: Int = 1,
        requestCode: Int,
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
            .showCropFrame(true)// 是否显示裁剪矩形边框 圆形裁剪时建议设为false
            .showCropGrid(false)// 是否显示裁剪矩形网格 圆形裁剪时建议设为false
            .rotateEnabled(false)
            .forResult(requestCode)
    }

    /**
     * 选择图片，压缩
     */
    fun selectPictureWithCompress(requestCode: Int) {
        if (!::pictureSelector.isInitialized) return
        map[requestCode] = Type.compress
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
            .forResult(requestCode)
    }

    fun initPictureSelector(activity: Activity): PictureAdapter {
        if (!::pictureSelector.isInitialized) {
            pictureSelector = PictureSelector.create(activity)
        }
        return this
    }

    fun initPictureSelector(fragment: Fragment): PictureAdapter {
        if (!::pictureSelector.isInitialized) {
            pictureSelector = PictureSelector.create(fragment)
        }
        return this
    }


}