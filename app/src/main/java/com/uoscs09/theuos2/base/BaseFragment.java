package com.uoscs09.theuos2.base;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.uoscs09.theuos2.util.TrackerUtil;

/**
 * 기본적인 편의 기능이 구현된 Fragment<br>
 * <br>
 * 지원되는 기능 : <br>
 * <li>{@link #getActionBar()} - 현 Activity 의 ActionBar 를 가져온다</> <li>
 * {@link #setSubtitleWhenVisible(CharSequence)} - Fragment 가 UI에 보여질 때,
 * subTitle 을 설정한다.</>
 */
public abstract class BaseFragment extends Fragment implements TrackerUtil.TrackerScreen {

    /**
     * 현 Activity 의 ActionBar 를 가져온다
     */
    protected final ActionBar getActionBar() {
        if (isAdded())
            return getAppCompatActivity().getSupportActionBar();
        else
            return null;
    }

    protected final AppCompatActivity getAppCompatActivity() {
        return (AppCompatActivity) getActivity();
    }

    protected final BaseActivity getBaseActivity() {
        return (BaseActivity) getActivity();
    }

    /**
     * Fragment 가 UI에 보여질 때, subTitle 을 설정한다
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

    /*
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            TrackerUtil.getInstance(this).sendVisibleEvent(getScreenNameForTracker());
        }
    }
    */

    /**
     * ActionBar 의 subTitle
     */
    @Nullable
    protected CharSequence getSubtitle() {
        return null;
    }

    public void sendTrackerEvent(String action, String label) {
        TrackerUtil.getInstance(this).sendEvent(getScreenNameForTracker(), action, label);
    }

    public void sendTrackerEvent(String action, String label, long value) {
        TrackerUtil.getInstance(this).sendEvent(getScreenNameForTracker(), action, label, value);
    }

    public void sendClickEvent(String label) {
        TrackerUtil.getInstance(this).sendClickEvent(getScreenNameForTracker(), label);
    }

    public void sendClickEvent(String label, long value) {
        TrackerUtil.getInstance(this).sendClickEvent(getScreenNameForTracker(), label, value);
    }

    protected void sendEmptyViewClickEvent() {
        sendClickEvent("emptyView");
    }

    @PermissionChecker.PermissionResult
    protected boolean checkSelfPermission(String permission) {
        return getBaseActivity().checkSelfPermissionCompat(permission);
    }

    // String...
    @PermissionChecker.PermissionResult
    protected boolean checkSelfPermissions(String[] permission) {
        return getBaseActivity().checkSelfPermissionCompat(permission);
    }

    protected boolean checkPermissionResultAndShowToastIfFailed(@NonNull String[] permissions, @NonNull int[] grantResults, String message) {
        return getBaseActivity().checkPermissionResultAndShowToastIfFailed(permissions, grantResults, message);
    }

    protected boolean checkPermissionResultAndShowToastIfFailed(@NonNull String[] permissions, @NonNull int[] grantResults, @StringRes int res) {
        return getBaseActivity().checkPermissionResultAndShowToastIfFailed(permissions, grantResults, res);
    }

    protected boolean checkPermissionResult(@NonNull String[] permissions, @NonNull int[] grantResults) {
        return getBaseActivity().checkPermissionResult(permissions, grantResults);
    }
}
