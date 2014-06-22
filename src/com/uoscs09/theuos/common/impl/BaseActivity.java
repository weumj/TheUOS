package com.uoscs09.theuos.common.impl;

import com.uoscs09.theuos.common.util.AppUtil;

import android.app.Activity;
import android.os.Bundle;
/**onCreate에서 테마 설정을 하는 액티비티*/
public class BaseActivity extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		AppUtil.applyTheme(this);
		super.onCreate(savedInstanceState);
	}
}
