package com.uoscs09.theuos.tab.timetable;


import android.os.Parcel;
import android.os.Parcelable;

import com.uoscs09.theuos.util.OApiUtil;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;

public class TimeTable implements Parcelable, Serializable {
    private static final long serialVersionUID = 5134424815398243634L;

    public OApiUtil.Semester semesterCode;
    public StudentInfo studentInfoKor, studentInfoEng;
    /**
     * period - day
     */
    public ArrayList<Subject[]> subjects = new ArrayList<>();
    public int year;

    public int maxTime = 0;

    public TimeTable() {
    }

    public TimeTable(int size) {
        for (int i = 0; i < size; i++) {
            subjects.add(new Subject[6]);
        }
    }

    public boolean isEmpty() {
        for (Subject[] subjectArray : subjects) {
            for (Subject subject : subjectArray) {
                if (subject != null && !subject.equals(Subject.EMPTY))
                    return false;
            }
        }

        return true;
    }

    public void setMaxTime(){
        maxTime = calculateLastExistPeriod();
    }

    private int calculateLastExistPeriod() {

        for (int i = subjects.size() - 1; i > -1; i--) {
            Subject[] subjectArray = subjects.get(i);
            for (Subject subject : subjectArray) {
                if (subject != null && !subject.equals(Subject.EMPTY))
                    return i;

            }

        }

        return 1;
    }

    public void copyFrom(TimeTable another) {
        this.subjects.clear();
        this.subjects.addAll(another.subjects);
        this.semesterCode = another.semesterCode;
        this.studentInfoKor = another.studentInfoKor;
        this.studentInfoEng = another.studentInfoEng;
        this.year = another.year;
        this.maxTime = another.maxTime;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(semesterCode != null ? semesterCode.intCode : 0);
        dest.writeInt(year);
        dest.writeParcelable(studentInfoKor, flags);
        dest.writeParcelable(studentInfoEng, flags);
        dest.writeInt(maxTime);
        dest.writeInt(subjects.size());
        for (int i = 0; i < subjects.size(); i++) {
            dest.writeParcelableArray(subjects.get(i), flags);
        }
    }

    public TimeTable(Parcel source) {
        semesterCode = OApiUtil.Semester.getSemesterFromCode(source.readInt());
        year = source.readInt();
        studentInfoKor = source.readParcelable(StudentInfo.class.getClassLoader());
        studentInfoEng = source.readParcelable(StudentInfo.class.getClassLoader());
        maxTime = source.readInt();
        int size = source.readInt();
        for (int i = 0; i < size; i++) {
            subjects.add((Subject[]) source.readParcelableArray(Subject.class.getClassLoader()));
        }

    }

    public static final Parcelable.Creator<TimeTable> CREATOR = new Parcelable.Creator<TimeTable>() {
        @Override
        public TimeTable[] newArray(int size) {
            return new TimeTable[size];
        }

        @Override
        public TimeTable createFromParcel(Parcel source) {
            return new TimeTable(source);
        }
    };

    public static class StudentInfo implements Parcelable, Serializable {
        private static final long serialVersionUID = 5402888948965878265L;
        private String name;
        private String studentCode;
        public String department;


        protected StudentInfo(Parcel in) {
            this.name = in.readString();
            this.studentCode = in.readString();
            this.department = in.readString();
        }

        public StudentInfo(String rawInfo) throws ParseException {
            int firstIndexOfParentheses = rawInfo.indexOf('(');
            int lastIndexOfParentheses = rawInfo.lastIndexOf(')');

            if (firstIndexOfParentheses == -1 || lastIndexOfParentheses == -1)
                throw new ParseException(rawInfo, 0);

            name = rawInfo.substring(0, firstIndexOfParentheses).trim();

            studentCode = rawInfo.substring(firstIndexOfParentheses + 1, lastIndexOfParentheses).trim();
            department = rawInfo.substring(lastIndexOfParentheses + 1, rawInfo.length()).trim();
        }


        @Override

        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(name);
            dest.writeString(studentCode);
            dest.writeString(department);
        }

        public static final Parcelable.Creator<StudentInfo> CREATOR = new Parcelable.Creator<StudentInfo>() {
            @Override
            public StudentInfo[] newArray(int size) {
                return new StudentInfo[size];
            }

            @Override
            public StudentInfo createFromParcel(Parcel source) {
                return new StudentInfo(source);
            }
        };
    }
}
