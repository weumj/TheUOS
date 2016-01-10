package com.uoscs09.theuos2.base;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import butterknife.ButterKnife;

/**
 * View Holder 패턴을 사용하는 ArrayAdapter
 */
public abstract class AbsArrayAdapter<T, VH extends AbsArrayAdapter.IViewHolder> extends ArrayAdapter<T> {
    protected int layoutId;

    public AbsArrayAdapter(Context context, int layout, List<T> list) {
        super(context, layout, list);
        this.layoutId = layout;
    }

    public AbsArrayAdapter(Context context, int layout, T[] array) {
        super(context, layout, array);
        this.layoutId = layout;
    }

    public AbsArrayAdapter(Context context, int layout) {
        super(context, layout);
        this.layoutId = layout;
    }

    public AbsArrayAdapter(Context context, int layout, int textViewID) {
        super(context, layout, textViewID);
        this.layoutId = layout;
    }

    public AbsArrayAdapter(Context context, int layout, int textViewID, List<T> list) {
        super(context, layout, textViewID, list);
        this.layoutId = layout;
    }

    @SuppressWarnings("unchecked")
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        VH holder;
        if (view == null) {
            view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
            holder = onCreateViewHolder(view, getItemViewType(position));
            view.setTag(holder);
        } else {
            holder = (VH) view.getTag();
        }

        onBindViewHolder(position, holder);

        return view;
    }

    /**
     * @param position 리스트의 위치
     * @param holder   getView 에서 설정되는 ViewHolder
     */
    public abstract void onBindViewHolder(int position, VH holder);

    /**
     * @param convertView ViewHolder 객체를 만드는데 사용될 View
     * @param viewType    viewType
     * @return AbsArrayAdapter 를 상속받은 클래스가 구현한 ViewHolder
     */
    public abstract VH onCreateViewHolder(View convertView, int viewType);


    public interface IViewHolder {
    }

    public static class ViewHolder implements IViewHolder {
        public final View itemView;

        public ViewHolder(View view) {
            this.itemView = view;
            ButterKnife.bind(this, view);
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

        public SimpleAdapter(Context context, int layout, int textViewId) {
            super(context, layout, textViewId);
        }

        public SimpleAdapter(Context context, int layout, int textViewId, List<T> list) {
            super(context, layout, textViewId, list);
        }

        public abstract String getTextFromItem(T item);

        @Override
        public void onBindViewHolder(int position, SimpleViewHolder holder) {
            holder.textView.setText(getTextFromItem(getItem(position)));
        }

        @Override
        public SimpleViewHolder onCreateViewHolder(View convertView, int viewType) {
            return new SimpleViewHolder(convertView);
        }
    }
}
