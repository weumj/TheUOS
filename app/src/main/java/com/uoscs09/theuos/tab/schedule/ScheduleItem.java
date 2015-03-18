package com.uoscs09.theuos.tab.schedule;

import android.os.Parcel;
import android.os.Parcelable;

public class ScheduleItem implements Parcelable {
	/** content, sch_date, year, month */
	public String[] stringArray;

	public ScheduleItem() {
		stringArray = new String[4];
	}

	/**
	 * @param array
	 *            content, sch_date, year, month
	 */
	public ScheduleItem(String[] array) {
		this();
		System.arraycopy(array, 0, stringArray, 0, stringArray.length);
	}

	protected ScheduleItem(Parcel in) {
		in.readStringArray(stringArray);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeStringArray(stringArray);
	}

	public static final Creator<ScheduleItem> CREATOR = new Creator<ScheduleItem>() {

		@Override
		public ScheduleItem createFromParcel(Parcel source) {
			return new ScheduleItem(source);
		}

		@Override
		public ScheduleItem[] newArray(int size) {
			return new ScheduleItem[size];
		}

	};
}
