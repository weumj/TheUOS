package com.uoscs09.theuos2.base;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * View Holder 패턴을 사용하는 ArrayAdapter
 */
public abstract class AbsArrayAdapter<T, VH extends AbsArrayAdapter.ViewHolderable> extends ArrayAdapter<T> {
    private final int layout;

    public AbsArrayAdapter(Context context, int layout, List<T> list) {
        super(context, layout, list);
        this.layout = layout;
    }

    public AbsArrayAdapter(Context context, int layout, T[] array) {
        super(context, layout, array);
        this.layout = layout;
    }

    public AbsArrayAdapter(Context context, int layout) {
        super(context, layout);
        this.layout = layout;
    }

    public AbsArrayAdapter(Context context, int layout, int textViewID) {
        super(context, layout, textViewID);
        this.layout = layout;
    }

    public AbsArrayAdapter(Context context, int layout, int textViewID, List<T> list) {
        super(context, layout, textViewID, list);
        this.layout = layout;
    }

    @SuppressWarnings("unchecked")
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        VH holder;
        if (view == null) {
            view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
            holder = getViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (VH) view.getTag();
        }

        onBindViewHolder(position, holder);

        return view;
    }

    /**
     * @param position 리스트의 위치
     * @param holder   getView에서 설정되는 ViewHolder
     */
    public abstract void onBindViewHolder(int position, VH holder);

    /**
     * @param convertView ViewHolder 객체를 만드는데 사용될 View
     * @return AbsArrayAdapter를 상속받은 클래스가 구현한 ViewHolder
     */
    public abstract VH getViewHolder(View convertView);

    public static interface ViewHolderable {

    }

    public static class ViewHolder implements ViewHolderable {
        public final View itemView;

        public ViewHolder(View view){
            this.itemView = view;
        }
    }

    public static class SimpleViewHolder extends ViewHolder {
        public final TextView textView;

        public SimpleViewHolder(View view) {
            super(view);
            textView = (TextView) view.findViewById(android.R.id.text1);
        }
    }


    public abstract static class SimpleAdapter<T> extends AbsArrayAdapter<T, SimpleViewHolder> {

        public SimpleAdapter(Context context, int layout, List<T> list) {
            super(context, layout, list);
        }

        public SimpleAdapter(Context context, int layout, T[] array) {
            super(context, layout, array);
        }

        public SimpleAdapter(Context context, int layout) {
            super(context, layout);
        }

        public SimpleAdapter(Context context, int layout, int textViewID) {
            super(context, layout, textViewID);
        }

        public SimpleAdapter(Context context, int layout, int textViewID, List<T> list) {
            super(context, layout, textViewID, list);
        }

        public abstract String getTextFromItem(T item);

        @Override
        public void onBindViewHolder(int position, SimpleViewHolder holder) {
            holder.textView.setText(getTextFromItem(getItem(position)));
        }

        @Override
        public SimpleViewHolder getViewHolder(View convertView) {
            return new SimpleViewHolder(convertView);
        }
    }
}
