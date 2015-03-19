package com.uoscs09.theuos.tab.transport;

import android.os.Parcel;
import android.os.Parcelable;

import com.uoscs09.theuos.util.StringUtil;

public class TransportItem implements Parcelable {
	public String stationCode;
	public String location;
	public String arrivalTime;
	public boolean isUpperLine;

	public TransportItem() {
		stationCode = location = arrivalTime = StringUtil.NULL;
		isUpperLine = false;
	}

	public TransportItem(String stationCode, String location, String arrivalTime) {
		this.stationCode = stationCode;
		this.arrivalTime = arrivalTime;
		this.location = location;
		isUpperLine = false;
	}

	protected TransportItem(Parcel p) {
		stationCode = p.readString();
		location = p.readString();
		arrivalTime = p.readString();
		boolean[] val = new boolean[1];
		p.readBooleanArray(val);
		isUpperLine = val[0];
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(stationCode);
		dest.writeString(location);
		dest.writeString(arrivalTime);
		dest.writeBooleanArray(new boolean[] { isUpperLine });
	}

	public static final Parcelable.Creator<TransportItem> CREATOR = new Creator<TransportItem>() {

		@Override
		public TransportItem[] newArray(int size) {
			return new TransportItem[size];
		}

		@Override
		public TransportItem createFromParcel(Parcel source) {
			return new TransportItem(source);
		}
	};
}
