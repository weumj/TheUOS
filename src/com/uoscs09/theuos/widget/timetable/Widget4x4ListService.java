package com.uoscs09.theuos.widget.timetable;

import android.content.Context;
import android.content.Intent;

public class Widget4x4ListService extends WidgetTimetableListService {

	@Override
	protected ListRemoteViewsFactory getListRemoteViewsFactory(Context context,
			Intent intent) {
		return new SmallSizeListRemoteViewsFactory(context, intent);
	}

	protected class SmallSizeListRemoteViewsFactory extends
			ListRemoteViewsFactory {
		public SmallSizeListRemoteViewsFactory(Context applicationContext,
				Intent intent) {
			super(applicationContext, intent);
		}

		@Override
		protected boolean isBigSize() {
			return false;
		}

	}
}
