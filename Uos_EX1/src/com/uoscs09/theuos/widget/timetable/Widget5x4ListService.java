package com.uoscs09.theuos.widget.timetable;

import android.content.Context;
import android.content.Intent;

public class Widget5x4ListService extends WidgetTimetableListService {

	@Override
	protected ListRemoteViewsFactory getListRemoteViewsFactory(Context context,
			Intent intent) {
		return new BigSizeListRemoteViewsFactory(context, intent);
	}

	protected class BigSizeListRemoteViewsFactory extends
			ListRemoteViewsFactory {

		public BigSizeListRemoteViewsFactory(Context applicationContext,
				Intent intent) {
			super(applicationContext, intent);
		}

		@Override
		protected boolean isBigSize() {
			return true;
		}

	}
}
