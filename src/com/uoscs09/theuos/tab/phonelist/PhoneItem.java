package com.uoscs09.theuos.tab.phonelist;

import com.uoscs09.theuos.common.util.StringUtil;

import android.os.Parcel;
import android.os.Parcelable;

public class PhoneItem implements Parcelable {
	public String siteName;
	public String sitePhoneNumber;

	public PhoneItem() {
		siteName = sitePhoneNumber = StringUtil.NULL;
	}

	public PhoneItem(String name, String PhoneNumber) {
		this.siteName = name;
		this.sitePhoneNumber = PhoneNumber;
	}

	protected PhoneItem(Parcel source) {
		byte b = source.readByte();
		if (b == 0) {
			siteName = sitePhoneNumber = null;
		} else {
			siteName = source.readString();
			sitePhoneNumber = source.readString();
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		if (siteName == null) {
			byte b = 0;
			dest.writeByte(b);
		} else {
			byte b = 1;
			dest.writeByte(b);
			dest.writeString(siteName);
			dest.writeString(sitePhoneNumber);
		}
	}

	public static final Parcelable.Creator<PhoneItem> CREATOR = new Parcelable.Creator<PhoneItem>() {

		@Override
		public PhoneItem[] newArray(int size) {
			return new PhoneItem[size];
		}

		@Override
		public PhoneItem createFromParcel(Parcel source) {
			return new PhoneItem(source);
		}
	};
}
