package com.uoscs09.theuos2.tab.score;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class ScoreItem implements Parcelable {
	public String title;
	public List<DetailScoreItem> list;

	public ScoreItem(String title, List<DetailScoreItem> list) {
		this.list = list;
		this.title = title;
	}
	
	protected ScoreItem(Parcel p) {
		title = p.readString();
		p.readList(list, DetailScoreItem.class.getClassLoader());
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(title);
		dest.writeList(list);
	}

	public static final Creator<ScoreItem> CREATOR = new Creator<ScoreItem>() {

		@Override
		public ScoreItem createFromParcel(Parcel source) {
			return new ScoreItem(source);
		}

		@Override
		public ScoreItem[] newArray(int size) {
			return new ScoreItem[size];
		}

	};
}
