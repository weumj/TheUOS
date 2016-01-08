package com.uoscs09.theuos2.setting;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.BaseActivity;

/**
 * 설정 activity, 주요 내용은 SettingsFragment 에 구현되어 있다.
 */
public class SettingActivity extends BaseActivity {


    CoordinatorLayout coordinatorLayout;
    private CoordinatorLayout.Behavior mAppBarBehavior;
    View mToolBarParent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        Toolbar toolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);

        if (Build.VERSION.SDK_INT > 20) {
            coordinatorLayout = (CoordinatorLayout) findViewById(R.id.activity_setting_coordinator);
            if(coordinatorLayout != null) {
                mToolBarParent = findViewById(R.id.toolbar_parent);
                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mToolBarParent.getLayoutParams();
                mAppBarBehavior = params.getBehavior();
            }
        }

        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.tabcontent, new SettingsFragment(), "main")
                .commit();
    }

    void restoreToolbar() {
        if (mAppBarBehavior != null) {
            //noinspection unchecked
            mAppBarBehavior.onNestedFling(coordinatorLayout, mToolBarParent, null, 0, -1000, true);
        }
    }

    @NonNull
    @Override
    public String getScreenNameForTracker() {
        return "SettingActivity";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return false;
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

}
