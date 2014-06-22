package com.uoscs09.theuos.tab.emptyroom;

import java.util.Comparator;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class ClassRoomItem implements Parcelable {
	public String[] array = new String[4];

	public ClassRoomItem(String building, String roomNo, String type,
			String availablePerson) {
		array[0] = building;
		array[1] = roomNo;
		array[2] = type;
		array[3] = availablePerson;
	}

	public ClassRoomItem(List<String> list) {
		int i = 0;
		for (String s : list) {
			array[i++] = s;
		}
	}

	protected ClassRoomItem(Parcel p) {
		p.readStringArray(array);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeStringArray(array);
	}

	public static final Parcelable.Creator<ClassRoomItem> CREATOR = new Parcelable.Creator<ClassRoomItem>() {

		@Override
		public ClassRoomItem createFromParcel(Parcel p) {
			return new ClassRoomItem(p);
		}

		@Override
		public ClassRoomItem[] newArray(int size) {
			return new ClassRoomItem[size];
		}

	};

	/**
	 * 리스트 정렬에 사용되는 Comparator 객체를 반환한다.
	 * 
	 * @param field
	 *            - 정렬 주체가 될 문자열 필드<br>
	 * */
	public static final Comparator<ClassRoomItem> getComparator(
			final int field, final boolean isReverse) {
		if (field > -1 && field < 3) {
			return new Comparator<ClassRoomItem>() {

				@Override
				public int compare(ClassRoomItem lhs, ClassRoomItem rhs) {
					if (isReverse) {
						return -(lhs.array[field].compareTo(rhs.array[field]));
					} else {
						return lhs.array[field].compareTo(rhs.array[field]);
					}
				}
			};
		} else if (field == 3) {
			return new Comparator<ClassRoomItem>() {

				@Override
				public int compare(ClassRoomItem lhs, ClassRoomItem rhs) {
					int l;
					try {
						l = Integer.valueOf(lhs.array[field]);
					} catch (NumberFormatException e) {
						l = Integer.MAX_VALUE;
					}
					int r;
					try {
						r = Integer.valueOf(rhs.array[field]);
					} catch (NumberFormatException e) {
						r = Integer.MAX_VALUE;
					}
					if (isReverse) {
						return r - l;
					} else {
						return l - r;
					}

				}
			};
		} else {
			return null;
		}
	}
}
