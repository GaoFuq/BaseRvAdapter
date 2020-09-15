package com.gfq.gdatabind;

import android.view.View;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

/**
 * ViewHolder基类
 */
public class BindingViewHolder<VB extends ViewDataBinding> extends RecyclerView.ViewHolder {
    public  VB getBinding() {
        return binding;
    }

    private final VB binding;

    public BindingViewHolder(View itemView) {
        super(itemView);
        binding = DataBindingUtil.bind(itemView);
    }

}
