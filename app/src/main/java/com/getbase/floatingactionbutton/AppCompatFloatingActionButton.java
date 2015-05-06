package com.getbase.floatingactionbutton;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.util.AttributeSet;

import com.uoscs09.theuos2.util.ImageUtil;

public class AppCompatFloatingActionButton extends FloatingActionButton {
    private Drawable mIconDrawable;
    @DrawableRes
    private int mIcon;

    public AppCompatFloatingActionButton(Context context) {
        this(context, null);
    }


    public AppCompatFloatingActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public AppCompatFloatingActionButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    void init(Context context, AttributeSet attributeSet) {

        TypedArray attr = context.obtainStyledAttributes(attributeSet, R.styleable.FloatingActionButton, 0, 0);
        mIcon = attr.getResourceId(R.styleable.FloatingActionButton_fab_icon, 0);
        attr.recycle();

        super.init(context, attributeSet);
    }

    @Override
    public void setIcon(int icon) {
        if (mIcon != icon) {
            mIcon = icon;
            mIconDrawable = null;
        }
        super.setIcon(icon);
    }

    @Override
    public void setIconDrawable(@NonNull Drawable iconDrawable) {
        if (mIconDrawable != iconDrawable) {
            mIcon = 0;
            mIconDrawable = iconDrawable;
        }
        super.setIconDrawable(iconDrawable);
    }

    @Override
    Drawable getIconDrawable() {
        if (mIconDrawable != null) {
            return mIconDrawable;
        } else if (mIcon != 0) {
            return ImageUtil.getDrawable(getContext(), mIcon);
        } else {
            return super.getIconDrawable();
        }
    }

}
