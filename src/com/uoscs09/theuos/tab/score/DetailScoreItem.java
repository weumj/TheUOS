package com.uoscs09.theuos.tab.score;

import android.os.Parcel;
import android.os.Parcelable;

public class DetailScoreItem implements Parcelable {
	public String type, class_eval_item, raw_score, eval_grade, ave;

	public DetailScoreItem(String type, String class_eval_item,
			String raw_score, String eval_grade, String ave) {
		this.type = type;
		this.class_eval_item = class_eval_item;
		this.raw_score = raw_score;
		this.eval_grade = eval_grade;
		this.ave = ave;
	}

	protected DetailScoreItem(Parcel p) {
		type = p.readString();
		class_eval_item = p.readString();
		raw_score = p.readString();
		eval_grade = p.readString();
		ave = p.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(type);
		dest.writeString(class_eval_item);
		dest.writeString(raw_score);
		dest.writeString(eval_grade);
		dest.writeString(ave);
	}

	public static final Creator<DetailScoreItem> CREATOR = new Creator<DetailScoreItem>() {

		@Override
		public DetailScoreItem createFromParcel(Parcel source) {
			return new DetailScoreItem(source);
		}

		@Override
		public DetailScoreItem[] newArray(int size) {
			return new DetailScoreItem[size];
		}

	};
}
