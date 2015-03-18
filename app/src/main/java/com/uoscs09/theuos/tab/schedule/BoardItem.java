package com.uoscs09.theuos.tab.schedule;

import android.os.Parcel;
import android.os.Parcelable;

public class BoardItem implements Parcelable {
	/** seq, notice_dt, title, content */
	public String[] stringArray = new String[4];

	public BoardItem() {
	}

	public BoardItem(String[] array) {
		System.arraycopy(array, 0, stringArray, 0, stringArray.length);
	}

	protected BoardItem(Parcel in) {
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

	public static final Creator<BoardItem> CREATOR = new Creator<BoardItem>() {

		@Override
		public BoardItem createFromParcel(Parcel source) {
			return new BoardItem(source);
		}

		@Override
		public BoardItem[] newArray(int size) {
			return new BoardItem[size];
		}

	};
}
