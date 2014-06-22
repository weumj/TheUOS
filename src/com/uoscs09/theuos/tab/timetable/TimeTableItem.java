package com.uoscs09.theuos.tab.timetable;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

import com.uoscs09.theuos.common.util.StringUtil;

public class TimeTableItem implements Parcelable, Serializable {
	private static final long serialVersionUID = -3271777988672447716L;
	public String time;
	public String mon;
	public String tue;
	public String wed;
	public String thr;
	public String fri;
	public String sat;

	public TimeTableItem() {
		time = mon = tue = wed = thr = fri = sat = StringUtil.NULL;
	}

	public TimeTableItem(TimeTableItem item) {
		this.time = item.time;
		this.mon = item.mon;
		this.tue = item.tue;
		this.wed = item.wed;
		this.thr = item.thr;
		this.fri = item.fri;
		this.sat = item.sat;
	}

	public TimeTableItem(String time, String mon, String tue, String wed,
			String thr, String fri, String sat) {
		this.time = time;
		this.mon = mon;
		this.tue = tue;
		this.wed = wed;
		this.thr = thr;
		this.fri = fri;
		this.sat = sat;
	}

	public TimeTableItem(String[] array) {
		this.time = array[0];
		this.mon = array[1];
		this.tue = array[2];
		this.wed = array[3];
		this.thr = array[4];
		this.fri = array[5];
		this.sat = array[6];
	}

	protected TimeTableItem(Parcel p) {
		this.time = p.readString();
		this.mon = p.readString();
		this.tue = p.readString();
		this.wed = p.readString();
		this.thr = p.readString();
		this.fri = p.readString();
		this.sat = p.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(time);
		dest.writeString(mon);
		dest.writeString(tue);
		dest.writeString(wed);
		dest.writeString(thr);
		dest.writeString(fri);
		dest.writeString(sat);
	}

	public static final Parcelable.Creator<TimeTableItem> CREATOR = new Parcelable.Creator<TimeTableItem>() {
		@Override
		public TimeTableItem[] newArray(int size) {
			return new TimeTableItem[size];
		}

		@Override
		public TimeTableItem createFromParcel(Parcel source) {
			return new TimeTableItem(source);
		}
	};
}
