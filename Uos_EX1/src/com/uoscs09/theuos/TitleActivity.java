package com.uoscs09.theuos;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class TitleActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_title);

		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				startActivity(new Intent(getApplicationContext(),
						PagerFragmentActivity.class));
				finish();
			}
		}, 500);
	}
}
