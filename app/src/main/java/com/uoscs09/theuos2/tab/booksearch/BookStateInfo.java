package com.uoscs09.theuos2.tab.booksearch;

import android.os.Parcel;
import android.os.Parcelable;

public class BookStateInfo implements Parcelable {
	/** bookCode, placeName, state */
	public String[] infoArray = new String[3];

	public BookStateInfo() {
	}

	public BookStateInfo(String placeName, String state, String bookCode) {
		infoArray[0] = bookCode;
		infoArray[1] = placeName;
		infoArray[2] = state;
	}

	BookStateInfo(Parcel source) {
		source.readStringArray(infoArray);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeStringArray(infoArray);
	}

	public static final Parcelable.Creator<BookStateInfo> CREATOR = new Parcelable.Creator<BookStateInfo>() {
		@Override
		public BookStateInfo[] newArray(int size) {
			return new BookStateInfo[size];
		}

		@Override
		public BookStateInfo createFromParcel(Parcel source) {
			return new BookStateInfo(source);
		}
	};
}