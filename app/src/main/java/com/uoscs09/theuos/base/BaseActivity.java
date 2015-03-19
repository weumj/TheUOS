package com.uoscs09.theuos.base;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import com.uoscs09.theuos.util.AppUtil;

/** onCreate에서 테마 설정을 하는 fragment액티비티 */
public abstract class BaseActivity extends ActionBarActivity {
	@Override
	protected void onCreate(Bundle arg0) {
		AppUtil.applyTheme(this);
		super.onCreate(arg0);
	}

	@Override
	protected void onDestroy() {
		AppUtil.unbindDrawables(getWindow().getDecorView());
		super.onDestroy();
		System.gc();
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
}
