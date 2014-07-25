package com.uoscs09.theuos.common.impl;

import com.uoscs09.theuos.common.util.AppUtil;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
/**onCreate에서 테마 설정을 하는 fragment액티비티*/
public class BaseFragmentActivity extends FragmentActivity{
	@Override
	protected void onCreate(Bundle arg0) {
		AppUtil.applyTheme(this);
		super.onCreate(arg0);
	}
}
