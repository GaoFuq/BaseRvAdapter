package com.gfq.baservadapter.photoalbum

import android.content.Intent
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.gfq.baservadapter.adapter.BaseVH
import com.gfq.baservadapter.refresh.get
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.decoration.GridSpacingItemDecoration

/**
 *  2022/5/13 15:46
 * @auth gaofuq
 * @description
 */
internal class Simple {
    lateinit var adapter: PhotoAlbumDefaultAdapter
    fun useSimple(activityOrFragment:Any,itemLayoutResId: Int = 0, recyclerView: RecyclerView) {

        val pictureSelector: PictureSelector = createPictureSelector(activityOrFragment)

        adapter = object : PhotoAlbumDefaultAdapter(this,
            itemLayoutResId,
            isShowGif = true,
            order = Order.reverseOrder) {
            override fun onBindAdder(holder: BaseVH, data: String) {
                holder.get<ViewDataBinding>().run {
//                    delete.gone()
//                    iv.setImage("http://mdwl-miyun.oss-cn-beijing.aliyuncs.com/image/0c1ec4f6f61140da8aa87ff559bc4db8.gif")
//                    iv.setDebounceClick { open() }
                }

            }

            override fun onBind(holder: BaseVH, data: String, position: Int) {
                holder.get<ViewDataBinding>().run {
//                    delete.visible()
//                    delete.setDebounceClick { removeAt(position) }
//                    iv.setImage(data)
//                    iv.setDebounceClick { pictureSelector.preview(position, dataList) }
                }
            }
        }

        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(GridSpacingItemDecoration(3, 30, true))
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
        val list = adapter.onActivityResult(requestCode, resultCode, data)
    }
}