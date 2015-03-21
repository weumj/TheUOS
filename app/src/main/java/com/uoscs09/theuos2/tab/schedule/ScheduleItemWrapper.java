package com.uoscs09.theuos2.tab.schedule;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class ScheduleItemWrapper implements Parcelable {
	public ArrayList<ScheduleItem> scheduleList;
	public ArrayList<BoardItem> boardList;

	public ScheduleItemWrapper() {
		scheduleList = new ArrayList<>();
		boardList = new ArrayList<>();
	}

	public ScheduleItemWrapper(ArrayList<ScheduleItem> scheduleList,
			ArrayList<BoardItem> boardList) {
		this.scheduleList = scheduleList;
		this.boardList = boardList;
	}

	protected ScheduleItemWrapper(Parcel in) {
		this();
		in.readList(boardList, BoardItem.class.getClassLoader());
		in.readList(scheduleList, ScheduleItem.class.getClassLoader());
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeList(boardList);
		dest.writeList(scheduleList);
	}

	public static final Creator<ScheduleItemWrapper> CREATOR = new Creator<ScheduleItemWrapper>() {

		@Override
		public ScheduleItemWrapper createFromParcel(Parcel source) {
			return new ScheduleItemWrapper(source);
		}

		@Override
		public ScheduleItemWrapper[] newArray(int size) {
			return new ScheduleItemWrapper[size];
		}

	};
}
