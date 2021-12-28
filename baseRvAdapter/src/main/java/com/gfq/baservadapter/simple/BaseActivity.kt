package com.gfq.baservadapter.simple

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

/**
 *  2021/12/23 11:26
 * @auth gaofuq
 * @description
 */
internal abstract class BaseActivity<Binding : ViewDataBinding> : AppCompatActivity() {
    lateinit var binding: Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<Binding>(this, layout())
        initView()
    }


    abstract fun layout(): Int

    abstract fun initView()

}