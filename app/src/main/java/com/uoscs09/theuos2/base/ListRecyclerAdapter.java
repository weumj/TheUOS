package com.uoscs09.theuos2.base;


import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;

public abstract class ListRecyclerAdapter<T, VH extends ListRecyclerAdapter.ViewHolder<T>> extends RecyclerView.Adapter<VH> {
    private final List<T> mDataSet;
    private OnItemClickListener<? extends VH> mListener;

    public ListRecyclerAdapter(List<T> list){
        this.mDataSet = list;
    }


    public void setOnItemClickListener(OnItemClickListener<? extends VH> listener) {
        this.mListener = listener;
    }

    @Override
    public void onBindViewHolder(ViewHolder h, int position) {
        h.mOnItemClickListener = mListener;

        h.item = mDataSet.get(position);

        h.setView();

    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public abstract static class ViewHolder<T> extends RecyclerView.ViewHolder implements View.OnClickListener{
        T item;
        private OnItemClickListener<ViewHolder> mOnItemClickListener;

        public ViewHolder(View itemView) {
            super(itemView);
        }

        protected abstract void setView();

        public final T getItem(){
            return item;
        }

        @Override
        public void onClick(View v) {
            if (mOnItemClickListener != null)
                mOnItemClickListener.onItemClick(this, v);
        }
    }
}
