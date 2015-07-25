package com.uoscs09.theuos2.tab.timetable;


import android.os.Parcel;
import android.os.Parcelable;

import com.uoscs09.theuos2.common.SerializableArrayMap;
import com.uoscs09.theuos2.parse.IParser;
import com.uoscs09.theuos2.tab.subject.SubjectItem2;
import com.uoscs09.theuos2.util.OApiUtil;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Locale;

public class TimeTable implements Parcelable, Serializable, IParser.AfterParsable {
    private static final long serialVersionUID = 5134424815398243634L;

    public OApiUtil.Semester semesterCode;
    public StudentInfo studentInfoKor, studentInfoEng;
    public static final int SUBJECT_AMOUNT_PER_WEEK = 6;

    /**
     * period - day
     */
    public ArrayList<Subject[]> subjects = new ArrayList<>();
    public int year;

    public int maxTime = 0;

    // Key - 과목 이름 hashCode, Value - 과목의 시간 & 장소 정보(SubjectItem2.ClassInformation 클래스)의 리스트
    private SerializableArrayMap<String, ArrayList<SubjectItem2.ClassInformation>> mClassInformationTable;


    public TimeTable() {
    }

    public TimeTable(int size) {
        for (int i = 0; i < size; i++) {
            subjects.add(new Subject[SUBJECT_AMOUNT_PER_WEEK]);
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

    public String getYearAndSemester() {
        if (semesterCode == null)
            return null;

        if (Locale.getDefault().equals(Locale.KOREA))
            return year + " / " + semesterCode.nameKor;
        else
            return semesterCode.nameEng + " / " + year;
    }

    public void setMaxTime() {
        maxTime = calculateLastExistPeriod();
    }

    private int calculateLastExistPeriod() {

        for (int i = subjects.size() - 1; i > -1; i--) {
            Subject[] subjectArray = subjects.get(i);
            for (Subject subject : subjectArray) {
                if (subject != null && !subject.isEqualsTo(Subject.EMPTY))
                    return i + 1;

            }

        }

        return 1;
    }

    public SerializableArrayMap<String, ArrayList<SubjectItem2.ClassInformation>> getClassTimeInformationTable() {
        if (mClassInformationTable == null) {
            mClassInformationTable = new SerializableArrayMap<>();
        }

        if (mClassInformationTable.size() == 0) {
            setMaxTime();

            // 날짜 선택
            for (int i = 0; i < 6; i++) {

                // 하나의 요일 계산 (세로줄)
                for (int j = 0; j < maxTime; j++) {
                    Subject[] subjectArray = subjects.get(j);

                    if (subjectArray.length <= i)
                        continue;

                    // 요일 - i , 시간 - j
                    Subject subject = subjectArray[i];
                    if (subject != null && !subject.isEqualsTo(Subject.EMPTY)) {
                        String key = subject.subjectName;

                        ArrayList<SubjectItem2.ClassInformation> infoList = mClassInformationTable.get(key);
                        if (infoList == null) {
                            infoList = new ArrayList<>();
                            mClassInformationTable.put(key, infoList);
                        }

                        SubjectItem2.ClassInformation info;
                        boolean needAdd = true;
                        if (!infoList.isEmpty()) {

                            int infoListSize = infoList.size();
                            for (int k = 0; k < infoListSize; k++) {
                                info = infoList.get(k);
                                if (info.dayInWeek == subject.day) {

                                    // 기존 저장된 장소, 날짜가 완전히 같으면, 시간만 추가한다.
                                    if (info.buildingAndRoom.equals(subject.building)) {
                                        // times 는 1부터 시작 (1교시)
                                        info.times.add(subject.period + 1);
                                        needAdd = false;
                                        break;
                                    }
                                    // 날짜만 같으면, 다음 항목에서 검사한다.
                                    // else {}

                                }
                                // 장소, 날짜가 모두 다르면, 다음 항목에서 검사한다.
                                // else {}

                                // 모두 검사해서 일치하는 항목이 없으면 새로 추가한다.
                            }

                        }

                        // 모두 검사해서 일치하는 항목이 없으면 새로 추가한다.
                        if (needAdd) {
                            info = new SubjectItem2.ClassInformation();
                            info.dayInWeek = subject.day;
                            info.buildingAndRoom += subject.building;
                            // times 는 1부터 시작 (1교시)
                            info.times.add(subject.period + 1);

                            infoList.add(info);
                        }

                    }
                }

            }


        }

        return mClassInformationTable;
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
    public void afterParsing() {
        getClassTimeInformationTable();
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

        int size = subjects.size();
        dest.writeInt(size);

        for (int i = 0; i < size; i++) {
            Subject[] subjects1 = subjects.get(i);

            if (subjects1 == null || subjects1.length == 0) {
                dest.writeInt(0);

            } else {
                dest.writeInt(1);
                dest.writeParcelableArray(subjects1, flags);
            }

        }

        dest.writeMap(mClassInformationTable);

    }

    private TimeTable(Parcel source) {
        semesterCode = OApiUtil.Semester.getSemesterByCode(source.readInt());
        year = source.readInt();
        studentInfoKor = source.readParcelable(StudentInfo.class.getClassLoader());
        studentInfoEng = source.readParcelable(StudentInfo.class.getClassLoader());
        maxTime = source.readInt();

        int size = source.readInt();
        for (int i = 0; i < size; i++) {
            if (source.readInt() == 1) {

                Parcelable[] parcelables = source.readParcelableArray(Subject.class.getClassLoader());
                Subject[] array = readParcelables(parcelables);
                subjects.add(array);

            } else
                subjects.add(new Subject[SUBJECT_AMOUNT_PER_WEEK]);
        }

        if (mClassInformationTable == null) {
            mClassInformationTable = new SerializableArrayMap<>();

        } else if(!mClassInformationTable.isEmpty())
            mClassInformationTable.clear();

        source.readMap(mClassInformationTable, SubjectItem2.ClassInformation.class.getClassLoader());

    }

    private Subject[] readParcelables(Parcelable[] parcelables) {
        Subject[] subjects = new Subject[parcelables.length];

        int i = 0;
        for (Parcelable p : parcelables) {
            Subject subject = (Subject) p;
            subjects[i++] = subject.isEqualsTo(Subject.EMPTY) ? Subject.EMPTY : subject;
        }

        return subjects;
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


        StudentInfo(Parcel in) {
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
