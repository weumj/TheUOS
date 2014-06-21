package com.uoscs09.theuos.tab.subject;

import java.util.Comparator;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.uoscs09.theuos.common.util.StringUtil;

/**
 * public String sub_dept<br>
 * public String subject_div<br>
 * public String subject_div2<br>
 * public String subject_no<br>
 * public String class_div<br>
 * public String subject_nm<br>
 * public String shyr<br>
 * public String credit<br>
 * public String prof_nm<br>
 * public String class_type<br>
 * public String class_nm<br>
 * public String tlsn_count<br>
 * public String tlsn_limit_count<br>
 */
public class SubjectItem implements Parcelable {
	// TODO 수업 시간을 String 이 아닌 날짜/시간 별 code로 변환
	public static final int SIZE = 13;
	public String[] infoArray = new String[SIZE];

	public SubjectItem() {
		for (int i = 0; i < SIZE; i++) {
			infoArray[i] = StringUtil.NULL;
		}
	}

	public SubjectItem(String[] array) {
		for (int i = 0; i < SIZE; i++) {
			infoArray[i] = array[i];
		}
	}

	public SubjectItem(List<String> list) {
		for (int i = 0; i < SIZE; i++) {
			infoArray[i] = list.get(i);
		}
	}

	public SubjectItem(String sub_dept, String subject_div,
			String subject_div2, String subject_no, String class_div,
			String subject_nm, String shyr, String credit, String prof_nm,
			String class_type, String class_nm, String tlsn_count,
			String tlsn_limit_count) {
		infoArray[0] = sub_dept;
		infoArray[1] = subject_div;
		infoArray[2] = subject_div2;
		infoArray[3] = subject_no;
		infoArray[4] = class_div;
		infoArray[5] = subject_nm;
		infoArray[6] = shyr;
		infoArray[7] = credit;
		infoArray[8] = prof_nm;
		infoArray[9] = class_type;
		infoArray[10] = class_nm;
		infoArray[11] = tlsn_count;
		infoArray[12] = tlsn_limit_count;
	}

	protected SubjectItem(Parcel parcel) {
		parcel.readStringArray(infoArray);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeStringArray(infoArray);
	}

	public static final Creator<SubjectItem> CREATOR = new Creator<SubjectItem>() {

		@Override
		public SubjectItem createFromParcel(Parcel source) {
			return new SubjectItem(source);
		}

		@Override
		public SubjectItem[] newArray(int size) {
			return new SubjectItem[size];
		}

	};

	public static final Comparator<SubjectItem> getComparator(final int field,
			final boolean isInverse) {
		switch (field) {
		case 0:
		case 1:
		case 2:
		case 5:
		case 7:
		case 8:
		case 9:
		case 10:
			return new Comparator<SubjectItem>() {
				@Override
				public int compare(SubjectItem lhs, SubjectItem rhs) {
					int result = lhs.infoArray[field]
							.compareTo(rhs.infoArray[field]);
					return isInverse ? -result : result;
				}
			};
		case 3:
		case 4:
		case 6:
		case 11:
		case 12:
			return new Comparator<SubjectItem>() {
				@Override
				public int compare(SubjectItem lhs, SubjectItem rhs) {
					int r, l;
					try {
						r = Integer.valueOf(rhs.infoArray[field]);
					} catch (Exception e) {
						r = Integer.MAX_VALUE;
					}
					try {
						l = Integer.valueOf(lhs.infoArray[field]);
					} catch (Exception e) {
						l = Integer.MAX_VALUE;
					}
					return isInverse ? r - l : l - r;
				}
			};
		default:
			return null;
		}
	}
}
