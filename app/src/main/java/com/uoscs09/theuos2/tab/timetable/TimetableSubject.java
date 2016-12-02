package com.uoscs09.theuos2.tab.timetable;

import android.os.Parcel;
import android.os.Parcelable;

import com.uoscs09.theuos2.util.OApiUtil;

import java.io.Serializable;
import java.util.Locale;

public class TimetableSubject implements Parcelable, Serializable {
    private static final long serialVersionUID = -5540327532775749251L;

    /**
     * 과목
     */
    public String subjectName = "";
    public String subjectNameEng = "";
    /** */
    public String subjectNameShort = "";
    public String subjectNameEngShort = "";

    /**
     * 교수
     */
    public String professor = "";
    public String professorEng = "";
    /**
     * 건물
     */
    public String building = "";
    public OApiUtil.UnivBuilding univBuilding;

    /**
     * 강의실
     */
    public String room = "";

    /**
     * 요일
     */
    public int day;
    /**
     * 교시
     */
    public int period;

    /**
     * * 파싱과정에서 정해진다.
     */
    public boolean isEqualToUpperPeriod = false;

    public TimetableSubject() {
    }

    public TimetableSubject(String raw, int day, int period, boolean isEng) {
        this.day = day;
        this.period = period;

        String[] splits = raw.split("\r");

        if (splits.length > 2) {
            if (isEng) {
                setSubjectNameEng(splits[0].trim());
                professorEng = splits[1].trim();

            } else {
                setSubjectName(splits[0].trim());
                professor = splits[1].trim();
            }

            setBuilding(splits[2]);
        }
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
        subjectNameShort = stringShort(subjectName, false);
    }

    public void setSubjectNameEng(String subjectNameEng) {
        this.subjectNameEng = subjectNameEng;
        subjectNameEngShort = stringShort(subjectNameEng, true);
    }

    public void setBuilding(String building) {
        this.building = building;

        String[] buildingSplitArray = building.split("-");

        if (buildingSplitArray.length > 1) {
            int buildingCode = Integer.parseInt(buildingSplitArray[0]);
            univBuilding = OApiUtil.UnivBuilding.fromNumber(buildingCode);

            room = buildingSplitArray[1].trim();
        }
    }

    private static String stringShort(String in, boolean isEng) {
        int max = isEng ? 12 : 5;
        return in.length() > max ? in.substring(0, max) + "..." : in;
    }

    public String getSubjectNameLocal() {
        return Locale.getDefault().equals(Locale.KOREA) ? subjectName : subjectNameEng;
    }

    public String getProfessorLocal() {
        return Locale.getDefault().equals(Locale.KOREA) ? professor : professorEng;
    }

    TimetableSubject(Parcel in) {
        setSubjectName(in.readString());
        setSubjectNameEng(in.readString());
        professor = in.readString();
        professorEng = in.readString();
        building = in.readString();
        room = in.readString();

        day = in.readInt();
        period = in.readInt();

        int i = in.readInt();

        if (i == -1)
            univBuilding = null;
        else
            univBuilding = OApiUtil.UnivBuilding.fromNumber(i);

        isEqualToUpperPeriod = in.readInt() == 1;
    }

    /**
     * 같은 과목인지 확인한다.
     */
    public boolean isEqualsTo(TimetableSubject another) {
        return this.professor.equals(another.professor) && this.subjectName.equals(another.subjectName);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(subjectName);
        dest.writeString(subjectNameEng);

        dest.writeString(professor);
        dest.writeString(professorEng);

        dest.writeString(building);
        dest.writeString(room);

        dest.writeInt(day);
        dest.writeInt(period);

        dest.writeInt(univBuilding == null ? -1 : univBuilding.code);

        dest.writeInt(isEqualToUpperPeriod ? 1 : 0);
    }

    public static final Parcelable.Creator<TimetableSubject> CREATOR = new Parcelable.Creator<TimetableSubject>() {
        @Override
        public TimetableSubject[] newArray(int size) {
            return new TimetableSubject[size];
        }

        @Override
        public TimetableSubject createFromParcel(Parcel source) {
            return new TimetableSubject(source);
        }
    };
}
