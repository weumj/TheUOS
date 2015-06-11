package com.uoscs09.theuos2.common;

import android.content.Context;
import android.util.AttributeSet;


public class NestedListView extends util.compatlibrary.widget.NestedListView {

    public NestedListView(Context context) {
        this(context, null);
    }

    public NestedListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NestedListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

}
