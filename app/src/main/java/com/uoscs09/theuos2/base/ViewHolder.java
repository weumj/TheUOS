package com.uoscs09.theuos2.base;

import android.view.View;

import butterknife.ButterKnife;
import mj.android.utils.recyclerview.ListRecyclerAdapter;

public class ViewHolder<T> extends ListRecyclerAdapter.ViewHolder<T> {
    public ViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    @Override
    protected void setView(int position) {

    }
}
