package com.uoscs09.theuos.tab.etc;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.impl.BaseFragmentActivity;
import com.uoscs09.theuos.tab.etc.score.ScoreFragment;

public class EtcActivity extends BaseFragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP
				| ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
		Fragment fragment;
		/*switch (getIntent().getIntExtra(TabEtcFragment.ETC_BUTTON, 0)) {
		case R.id.tab_etc_button_score:
			fragment = Fragment.instantiate(getApplicationContext(),
					ScoreFragment.class.getName());
			actionBar.setTitle(R.string.title_tab_score);
			break;
		default:
			finish();
			return;
		}*/
		//getSupportFragmentManager().beginTransaction()
		//		.replace(android.R.id.content, fragment).commit();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		default:
			return false;
		}
	}
}
