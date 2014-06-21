package com.uoscs09.theuos.widget.timetable;

import android.content.Context;
import android.widget.RemoteViews;

import com.uoscs09.theuos.R;

public class TimeTableWidget5x4 extends TimeTableWidget {
	@Override
	protected synchronized RemoteViews getRemoteViews(Context context) {
		return new RemoteViews(context.getPackageName(),
				R.layout.widget_timetable_5x4);
	}

	@Override
	protected Class<? extends WidgetTimetableListService> getListServiceClass() {
		return Widget5x4ListService.class;
	}

	@Override
	protected Class<? extends TimeTableWidget> getWidgetClass() {
		return this.getClass();
	}

}
