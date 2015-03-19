package com.uoscs09.theuos.tab.timetable;

import android.os.Parcel;
import android.os.Parcelable;

import com.uoscs09.theuos.util.StringUtil;

import java.io.Serializable;
import java.util.Locale;

public class Subject implements Parcelable, Serializable {
    private static final long serialVersionUID = -5540327532775749251L;

    public static final Subject EMPTY = new Subject();

    /**
     * 과목
     */
    public String subjectName = StringUtil.NULL;
    public String subjectNameEng = StringUtil.NULL;
    /** */
    public String subjectNameShort = StringUtil.NULL;
    public String subjectNameEngShort = StringUtil.NULL;

    /**
     * 교수
     */
    public String professor = StringUtil.NULL;
    public String professorEng = StringUtil.NULL;
    /**
     * 건물
     */
    public String building = StringUtil.NULL;

    public String buildingName = StringUtil.NULL;
    public String buildingNameEng = StringUtil.NULL;
    /**
     * 건물 (숫자)
     */
    public int buildingCode = 0;
    /**
     * 강의실
     */
    public String room = StringUtil.NULL;

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

    public Subject() {
    }

    public Subject(String raw, int day, int period, boolean isEng) {
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
            buildingCode = Integer.valueOf(buildingSplitArray[0]);
            room = buildingSplitArray[1].trim();
        }
    }

    private static String stringShort(String in, boolean isEng) {
        int max = isEng ? 12 : 5;
        return in.length() > max ? in.substring(0, max) + "..." : in;
    }

    public String getSubjectNameLocal(){
        return Locale.getDefault().equals(Locale.KOREA) ? subjectName : subjectNameEng;
    }

    public String getProfessorLocal(){
        return Locale.getDefault().equals(Locale.KOREA) ? professor : professorEng;
    }

    protected Subject(Parcel in) {
        setSubjectName(in.readString());
        setSubjectNameEng(in.readString());
        professor = in.readString();
        professorEng = in.readString();
        building = in.readString();
        buildingCode = in.readInt();
        room = in.readString();

        day = in.readInt();
        period = in.readInt();

        buildingName = in.readString();
        buildingNameEng = in.readString();

        isEqualToUpperPeriod = in.readInt() == 1;
    }

    /**
     * 같은 과목인지 확인한다.
     */
    public boolean isEqualsTo(Subject another) {
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
        dest.writeInt(buildingCode);
        dest.writeString(room);

        dest.writeInt(day);
        dest.writeInt(period);

        dest.writeString(buildingName);
        dest.writeString(buildingNameEng);

        dest.writeInt(isEqualToUpperPeriod ? 1 : 0);
    }

    public static final Parcelable.Creator<Subject> CREATOR = new Parcelable.Creator<Subject>() {
        @Override
        public Subject[] newArray(int size) {
            return new Subject[size];
        }

        @Override
        public Subject createFromParcel(Parcel source) {
            return new Subject(source);
        }
    };
}
