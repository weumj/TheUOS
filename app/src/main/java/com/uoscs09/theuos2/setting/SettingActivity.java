package com.uoscs09.theuos2.setting;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.BaseActivity;

/**
 * 설정 activity, 주요 내용은 SettingsFragment 에 구현되어 있다.
 */
public class SettingActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        Toolbar toolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.tabcontent, new SettingsFragment(), "main")
                .commit();
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

    @Override
    public void onBackPressed() {
        // support v4와 일반 Api 와의 호환성을 위해 구현
        if (!getFragmentManager().popBackStackImmediate()) {
            super.onBackPressed();
        }
    }

}
