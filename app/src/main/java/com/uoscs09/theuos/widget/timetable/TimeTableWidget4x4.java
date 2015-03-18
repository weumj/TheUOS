package com.uoscs09.theuos.widget.timetable;

import android.content.Context;
import android.widget.RemoteViews;

import com.uoscs09.theuos.R;

public class TimeTableWidget4x4 extends TimeTableWidget {

	@Override
	protected synchronized RemoteViews getRemoteViews(Context context) {
		return new RemoteViews(context.getPackageName(),
					R.layout.widget_timetable_4x4);
	}

	@Override
	protected Class<? extends WidgetTimetableListService> getListServiceClass() {
		return Widget4x4ListService.class;
	}

	@Override
	protected Class<? extends TimeTableWidget> getWidgetClass() {
		return this.getClass();
	}
}
