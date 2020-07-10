package com.gfq.gdatabind;

import android.view.View;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

/**
 * ViewHolder基类
 */
public class SuperBindingViewHolder<T extends ViewDataBinding> extends RecyclerView.ViewHolder {
    private boolean isNotifyDdit=true;

    public boolean isNotifyDdit() {
        return isNotifyDdit;
    }

    public void setNotifyDdit(boolean notifyDdit) {
        isNotifyDdit = notifyDdit;
    }

    public  T getBinding() {
        return binding;
    }

    private final T binding;

    public SuperBindingViewHolder(View itemView) {
        super(itemView);
        binding = DataBindingUtil.bind(itemView);
    }

}
