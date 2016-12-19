package com.uoscs09.theuos2.appwidget.timetable;

import android.content.Context;
import android.content.Intent;

public class Widget4x4ListService extends WidgetTimeTableListService2 {

	@Override
	protected ListRemoteViewsFactory getListRemoteViewsFactory(Context context,	Intent intent) {
		return new SmallSizeListRemoteViewsFactory(context, intent);
	}

	protected static class SmallSizeListRemoteViewsFactory extends ListRemoteViewsFactory {
		SmallSizeListRemoteViewsFactory(Context applicationContext, Intent intent) {
			super(applicationContext, intent);
		}

		@Override
		protected boolean isBigSize() {
			return false;
		}

	}
}
