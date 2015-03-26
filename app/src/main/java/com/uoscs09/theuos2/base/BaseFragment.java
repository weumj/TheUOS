package com.uoscs09.theuos2.base;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.TrackerUtil;

/**
 * 기본적인 편의 기능이 구현된 Fragment<br>
 * <br>
 * 지원되는 기능 : <br>
 * <li>{@link #getActionBar()} - 현 Activity의 ActionBar를 가져온다</> <li>
 * {@link #setSubtitleWhenVisible(CharSequence)} - Fragment가 UI에 보여질 때,
 * subTitle을 설정한다.</>
 */
public abstract class BaseFragment extends Fragment {

    @Override
    public void onDetach() {
        AppUtil.releaseResource(this);
        super.onDetach();
        System.gc();
    }

    /**
     * 현 Activity의 ActionBar를 가져온다
     */
    protected final ActionBar getActionBar() {
        if (isAdded())
            return ((ActionBarActivity) getActivity()).getSupportActionBar();
        else
            return null;
    }

    /**
     * Fragment가 UI에 보여질 때, subTitle을 설정한다
     */
    protected void setSubtitleWhenVisible(CharSequence subTitle) {
        if (isMenuVisible()) {
            if (getActionBar() != null)
                getActionBar().setSubtitle(subTitle);
        }
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if (menuVisible) {
            if (getActionBar() != null)
                getActionBar().setSubtitle(getSubtitle());

        } else {
            if (getActionBar() != null)
                getActionBar().setSubtitle(null);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            TrackerUtil.getInstance(this).sendVisibleEvent(getFragmentNameForTracker());
        }
    }

    /**
     * ActionBar의 subTitle
     */
    @Nullable
    protected CharSequence getSubtitle() {
        return null;
    }


    @NonNull
    protected abstract String getFragmentNameForTracker();

    protected void sendTrackerEvent(String action, String label) {
        TrackerUtil.getInstance(this).sendEvent(getFragmentNameForTracker(), action, label);
    }

    protected void sendTrackerEvent(String action, String label, long value) {
        TrackerUtil.getInstance(this).sendEvent(getFragmentNameForTracker(), action, label, value);
    }

    protected void sendClickEvent(String label) {
        TrackerUtil.getInstance(this).sendClickEvent(getFragmentNameForTracker(), label);
    }

    protected void sendClickEvent(String label, long value) {
        TrackerUtil.getInstance(this).sendClickEvent(getFragmentNameForTracker(), label, value);
    }

    protected void sendEmptyViewClickEvent() {
        sendClickEvent("emptyView");
    }
}
