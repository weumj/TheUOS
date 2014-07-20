package com.uoscs09.theuos.tab.restaurant;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

public class RestItem implements Parcelable, Serializable {
	private static final long serialVersionUID = 8809096102781534332L;
	public String title;
	public String body;
	public String breakfast;
	public String lunch;
	public String supper;

	public RestItem() {
		title = body = breakfast = lunch = supper = "";
	}

	public RestItem(String title, String body, String breakfast, String lunch,
			String supper) {
		this.title = title;
		this.body = body;
		this.breakfast = breakfast;
		this.lunch = lunch;
		this.supper = supper;
	}

	protected RestItem(Parcel source) {
		title = source.readString();
		body = source.readString();
		breakfast = source.readString();
		lunch = source.readString();
		supper = source.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(title);
		dest.writeString(body);
		dest.writeString(breakfast);
		dest.writeString(lunch);
		dest.writeString(supper);
	}

	public static final Creator<RestItem> CREATOR = new Creator<RestItem>() {

		@Override
		public RestItem createFromParcel(Parcel source) {
			return new RestItem(source);
		}

		@Override
		public RestItem[] newArray(int size) {
			return new RestItem[size];
		}

	};
}
