package com.uoscs09.theuos2.tab.timetable;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.uoscs09.theuos2.common.SerializableArrayMap;
import com.uoscs09.theuos2.tab.subject.Subject;
import com.uoscs09.theuos2.util.OApiUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

//todo DB
public class Timetable2 implements Parcelable, Serializable {

    private static final long serialVersionUID = 7330044121618353219L;

    public Timetable2(OApiUtil.Semester semester, int year, StudentInfo studentInfo,
                      List<Period> periods, SerializableArrayMap<String, Integer> colorTable,
                      int maxPeriod, SerializableArrayMap<String, ArrayList<Subject.ClassInformation>> classInformationTable) {
        this.semester = semester;
        this.year = year;
        this.studentInfo = studentInfo;
        this.periods = periods;
        this.colorTable = colorTable;
        this.maxPeriod = maxPeriod;
        this.classInformationTable = classInformationTable;

        buildLayoutInfo();
    }

    private OApiUtil.Semester semester;
    private int year;

    private StudentInfo studentInfo;
    private List<Period> periods;

    private SerializableArrayMap<String, Integer> colorTable;

    private ArrayList<LayoutInfo> layoutInfo;


    // Key - 과목 이름 hashCode, Value - 과목의 시간 & 장소 정보(Subject.ClassInformation 클래스)의 리스트
    private SerializableArrayMap<String, ArrayList<Subject.ClassInformation>> classInformationTable;

    private int maxPeriod;

    public OApiUtil.Semester semester() {
        return semester;
    }

    public int year() {
        return year;
    }

    public ArrayList<LayoutInfo> layoutInfo() {
        return layoutInfo;
    }

    @Nullable
    public StudentInfo studentInfo() {
        return studentInfo;
    }

    public List<Period> periods() {
        return periods;
    }

    public int maxPeriod() {
        return maxPeriod;
    }

    @SuppressLint("DefaultLocale")
    public String getYearAndSemester() {
        return isLocaleKor() ? String.format("%d / %s", year, semester.nameKor) : String.format("%s / %d", semester.nameEng, year);
    }

    public Map<String, ArrayList<Subject.ClassInformation>> classTimeInformationTable() {
        return classInformationTable;
    }

    public SerializableArrayMap<String, Integer> colorTable() {
        return colorTable;
    }

    public int color(SubjectInfo subjectInfo) {
        if (subjectInfo == null)
            return -1;

        Integer integer = colorTable.get(subjectInfo.name);

        if (integer == null)
            return -1;
        else
            return integer;
    }

    public static class StudentInfo implements Parcelable, Serializable {
        private static final long serialVersionUID = 2205625515617872043L;

        public StudentInfo(String name, String nameEng, int number, String department, String departmentEng) {
            this.name = name;
            this.nameEng = nameEng;
            this.number = number;
            this.department = department;
            this.departmentEng = departmentEng;
        }

        private String name;
        private String nameEng;

        private int number;

        private String department;
        private String departmentEng;

        public String name() {
            return isLocaleKor() ? name : nameEng;
        }

        public String department() {
            return isLocaleKor() ? department : departmentEng;
        }

        public int number() {
            return number;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.name);
            dest.writeString(this.nameEng);
            dest.writeInt(this.number);
            dest.writeString(this.department);
            dest.writeString(this.departmentEng);
        }

        protected StudentInfo(Parcel in) {
            this.name = in.readString();
            this.nameEng = in.readString();
            this.number = in.readInt();
            this.department = in.readString();
            this.departmentEng = in.readString();
        }

        public static final Parcelable.Creator<StudentInfo> CREATOR = new Parcelable.Creator<StudentInfo>() {
            @Override
            public StudentInfo createFromParcel(Parcel source) {
                return new StudentInfo(source);
            }

            @Override
            public StudentInfo[] newArray(int size) {
                return new StudentInfo[size];
            }
        };
    }

    public static class Period implements Parcelable, Serializable {
        private static final long serialVersionUID = -498880121430422262L;

        public Period(String period, String periodEng, String time, SubjectInfo[] subjectInformation) {
            this.period = period;
            this.periodEng = periodEng;
            this.time = time;
            this.subjectInformation = subjectInformation;
        }

        private String period;
        private String periodEng;

        private String time;

        SubjectInfo[] subjectInformation;


        public String period() {
            return isLocaleKor() ? period : periodEng;
        }

        public String time() {
            return time;
        }

        /**
         * @param day mon - 0 , sat - 5
         */
        @Nullable
        public SubjectInfo getSubjectInfo(int day) {
            if (day > 6 || day < 0)
                return null;

            return subjectInformation[day];
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.period);
            dest.writeString(this.periodEng);
            dest.writeString(this.time);
            dest.writeTypedArray(this.subjectInformation, flags);
        }

        protected Period(Parcel in) {
            this.period = in.readString();
            this.periodEng = in.readString();
            this.time = in.readString();
            this.subjectInformation = in.createTypedArray(SubjectInfo.CREATOR);
        }

        public static final Parcelable.Creator<Period> CREATOR = new Parcelable.Creator<Period>() {
            @Override
            public Period createFromParcel(Parcel source) {
                return new Period(source);
            }

            @Override
            public Period[] newArray(int size) {
                return new Period[size];
            }
        };
    }

    public static class SubjectInfo implements Parcelable, Serializable {

        private static final long serialVersionUID = -286446442932911656L;

        public SubjectInfo(String name, String nameEng, String professor, String professorEng,
                           String location, String locationEng, OApiUtil.UnivBuilding building,
                           boolean equalPrior) {
            this.name = name;
            this.nameEng = nameEng;
            this.professor = professor;
            this.professorEng = professorEng;
            this.location = location;
            this.locationEng = locationEng;
            this.building = building;
            this.equalPrior = equalPrior;
        }

        private String name;
        private String nameEng;

        private String professor;
        private String professorEng;

        private String location;
        private String locationEng;

        private OApiUtil.UnivBuilding building;

        private boolean equalPrior;

        public String name() {
            return isLocaleKor() ? name : nameEng;
        }

        String nameKor() {
            return name;
        }

        public String professor() {
            return isLocaleKor() ? professor : professorEng;
        }

        public String location() {
            return isLocaleKor() ? location : locationEng;
        }

        @Nullable
        public OApiUtil.UnivBuilding building() {
            return building;
        }

        public boolean isEqualPrior() {
            return equalPrior;
        }

        @Override
        public int hashCode() {
            int result = 17;
            result = 31 * result + name.hashCode();
            result = 31 * result + nameEng.hashCode();
            result = 31 * result + professor.hashCode();
            result = 31 * result + professorEng.hashCode();

            result = 31 * result + location.hashCode();
            result = 31 * result + locationEng.hashCode();

            result = 31 * result + building.hashCode();

            return result;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.name);
            dest.writeString(this.nameEng);
            dest.writeString(this.professor);
            dest.writeString(this.professorEng);
            dest.writeString(this.location);
            dest.writeString(this.locationEng);
            dest.writeInt(this.building == null ? -1 : this.building.ordinal());
            dest.writeByte(equalPrior ? (byte) 1 : (byte) 0);
        }

        protected SubjectInfo(Parcel in) {
            this.name = in.readString();
            this.nameEng = in.readString();
            this.professor = in.readString();
            this.professorEng = in.readString();
            this.location = in.readString();
            this.locationEng = in.readString();
            int tmpBuilding = in.readInt();
            this.building = tmpBuilding == -1 ? null : OApiUtil.UnivBuilding.values()[tmpBuilding];
            this.equalPrior = in.readByte() != 0;
        }

        public static final Creator<SubjectInfo> CREATOR = new Creator<SubjectInfo>() {
            @Override
            public SubjectInfo createFromParcel(Parcel source) {
                return new SubjectInfo(source);
            }

            @Override
            public SubjectInfo[] newArray(int size) {
                return new SubjectInfo[size];
            }
        };
    }

    private static boolean isLocaleKor() {
        return Locale.getDefault().equals(Locale.KOREA);
    }


    private void buildLayoutInfo() {
        layoutInfo = new ArrayList<>();
        for (int day = 0; day < 6; day++) {
            for (int i = 0; i < periods.size(); i++) {
                Period period = periods.get(i);

                SubjectInfo subjectInfo = period.subjectInformation[day];
                if (subjectInfo != null && !TextUtils.isEmpty(subjectInfo.name)) {
                    LayoutInfo info = new LayoutInfo(subjectInfo, day, i, 1);
                    int idx = layoutInfo.indexOf(info);
                    if (idx != -1) {
                        info = layoutInfo.get(idx);
                        info.size++;
                    } else {
                        layoutInfo.add(info);
                    }
                }
            }
        }
    }

    public static class LayoutInfo implements Parcelable, Serializable {
        private static final long serialVersionUID = -5173287275678422079L;
        private SubjectInfo subjectInfo;
        private int day;

        public int day() {
            return day;
        }

        public int period() {
            return period;
        }

        private int period;

        @Override
        public int hashCode() {
            int result = 17;

            result = 31 * result + subjectInfo.hashCode();
            result = 31 * result + day;

            return result;
        }

        private int size;

        public LayoutInfo(SubjectInfo subjectInfo, int day, int period, int size) {
            this.subjectInfo = subjectInfo;
            this.day = day;
            this.period = period;
            this.size = size;
        }

        public SubjectInfo subjectInfo() {
            return subjectInfo;
        }

        public int color(Timetable2 timetable) {
            return timetable.colorTable.get(subjectInfo.name);
        }


        public int size() {
            return size;
        }

        @Override
        public boolean equals(Object o) {
            if (o != null && o instanceof LayoutInfo) {
                LayoutInfo a = (LayoutInfo) o;
                return this.subjectInfo.name.equals(a.subjectInfo.name) && this.day == a.day;
            }
            return super.equals(o);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(this.subjectInfo, flags);
            dest.writeInt(this.day);
            dest.writeInt(this.period);
            dest.writeInt(this.size);
        }

        protected LayoutInfo(Parcel in) {
            this.subjectInfo = in.readParcelable(SubjectInfo.class.getClassLoader());
            this.day = in.readInt();
            this.period = in.readInt();
            this.size = in.readInt();
        }

        public static final Creator<LayoutInfo> CREATOR = new Creator<LayoutInfo>() {
            @Override
            public LayoutInfo createFromParcel(Parcel source) {
                return new LayoutInfo(source);
            }

            @Override
            public LayoutInfo[] newArray(int size) {
                return new LayoutInfo[size];
            }
        };
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.semester == null ? -1 : this.semester.ordinal());
        dest.writeInt(this.year);
        dest.writeParcelable(this.studentInfo, flags);
        dest.writeTypedList(periods);
        dest.writeSerializable(this.colorTable);
        dest.writeTypedList(layoutInfo);
        dest.writeSerializable(this.classInformationTable);
        dest.writeInt(this.maxPeriod);
    }

    @SuppressWarnings("unchecked")
    protected Timetable2(Parcel in) {
        int tmpSemester = in.readInt();
        this.semester = tmpSemester == -1 ? null : OApiUtil.Semester.values()[tmpSemester];
        this.year = in.readInt();
        this.studentInfo = in.readParcelable(StudentInfo.class.getClassLoader());
        this.periods = in.createTypedArrayList(Period.CREATOR);
        this.colorTable = (SerializableArrayMap<String, Integer>) in.readSerializable();
        this.layoutInfo = in.createTypedArrayList(LayoutInfo.CREATOR);
        this.classInformationTable = (SerializableArrayMap<String, ArrayList<Subject.ClassInformation>>) in.readSerializable();
        this.maxPeriod = in.readInt();
    }

    public static final Creator<Timetable2> CREATOR = new Creator<Timetable2>() {
        @Override
        public Timetable2 createFromParcel(Parcel source) {
            return new Timetable2(source);
        }

        @Override
        public Timetable2[] newArray(int size) {
            return new Timetable2[size];
        }
    };
}
