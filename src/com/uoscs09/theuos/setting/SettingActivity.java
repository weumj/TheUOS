package com.uoscs09.theuos.setting;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.uoscs09.theuos.common.impl.BaseActivity;
/** 설정 activity, 주요 내용은 SettingsFragment에 구현되어 있다.*/
public class SettingActivity extends BaseActivity {  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new SettingsFragment()).commit();
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
}
