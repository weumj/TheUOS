package com.uoscs09.theuos2.tab.score;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* 성적*/
public class WiseScores implements Parcelable {

    /*
    int scoreCulture; // 교양
    int scoreMajor; // 전필
    int scoreMajorOption; // 전선
*/

    Map<String, String> scoreMap;
    int scoreEarnedTotal; // 취득학점
    float evalSumTotal; // 평점합계
    float evalAverageTotal; // 평점평균
    float evalPercentTotal; // 백분율 환산

    List<SemesterScore> semesterScores;

    /* 학기 성적*/
    public static class SemesterScore implements Parcelable {
        /*
        int year;
        int semester;
        */
        String yearAndSemester;
        int scoreEarned; // 취득학점
        float evalSum; // 평점합계
        float evalAverage; // 평점평균
        float evalPercent; // 백분율 환산

        List<SubjectScore> subjectScores;

        @Override
        public int hashCode() {
            int result = 17;
            result = 31 * result + (yearAndSemester == null ? 0 : yearAndSemester.hashCode());
            result = 31 * result + scoreEarned;
            result = 31 * result + Float.floatToIntBits(evalSum);
            result = 31 * result + Float.floatToIntBits(evalAverage);
            result = 31 * result + Float.floatToIntBits(evalPercent);
            result = 31 * result + (subjectScores == null ? 0 : subjectScores.hashCode());
            return result;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.yearAndSemester);
            dest.writeInt(this.scoreEarned);
            dest.writeFloat(this.evalSum);
            dest.writeFloat(this.evalAverage);
            dest.writeFloat(this.evalPercent);
            dest.writeTypedList(this.subjectScores);
        }

        public SemesterScore() {
        }

        protected SemesterScore(Parcel in) {
            this.yearAndSemester = in.readString();
            this.scoreEarned = in.readInt();
            this.evalSum = in.readFloat();
            this.evalAverage = in.readFloat();
            this.evalPercent = in.readFloat();
            this.subjectScores = in.createTypedArrayList(SubjectScore.CREATOR);
        }

        public static final Parcelable.Creator<SemesterScore> CREATOR = new Parcelable.Creator<SemesterScore>() {
            @Override
            public SemesterScore createFromParcel(Parcel source) {
                return new SemesterScore(source);
            }

            @Override
            public SemesterScore[] newArray(int size) {
                return new SemesterScore[size];
            }
        };
    }

    /* 과목 성적*/
    public static class SubjectScore implements Parcelable {
        int order;
        String name;
        String subjectDiv; // 교과 구분
        int credit; // 학점
        String grade; // 등급
        float eval;// 평점
        boolean valid; // 성적 유효 여부
        boolean retaking; // 재수강 여부
        String retakingSubjectName;


        @Override
        public int hashCode() {
            int result = 17;
            result = 31 * result + order;
            result = 31 * result + (name == null ? 0 : name.hashCode());
            result = 31 * result + (subjectDiv == null ? 0 : subjectDiv.hashCode());
            result = 31 * result + credit;
            result = 31 * result + (grade == null ? 0 : grade.hashCode());
            result = 31 * result + Float.floatToIntBits(eval);
            result = 31 * result + (valid ? 1 : 0);
            result = 31 * result + (retaking ? 1 : 0);
            result = 31 * result + (retakingSubjectName == null ? 0 : retakingSubjectName.hashCode());
            return result;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.order);
            dest.writeString(this.name);
            dest.writeString(this.subjectDiv);
            dest.writeInt(this.credit);
            dest.writeString(this.grade);
            dest.writeFloat(this.eval);
            dest.writeByte(this.valid ? (byte) 1 : (byte) 0);
            dest.writeByte(this.retaking ? (byte) 1 : (byte) 0);
            dest.writeString(this.retakingSubjectName);
        }

        public SubjectScore() {
        }

        protected SubjectScore(Parcel in) {
            this.order = in.readInt();
            this.name = in.readString();
            this.subjectDiv = in.readString();
            this.credit = in.readInt();
            this.grade = in.readString();
            this.eval = in.readFloat();
            this.valid = in.readByte() != 0;
            this.retaking = in.readByte() != 0;
            this.retakingSubjectName = in.readString();
        }

        public static final Parcelable.Creator<SubjectScore> CREATOR = new Parcelable.Creator<SubjectScore>() {
            @Override
            public SubjectScore createFromParcel(Parcel source) {
                return new SubjectScore(source);
            }

            @Override
            public SubjectScore[] newArray(int size) {
                return new SubjectScore[size];
            }
        };
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.scoreMap.size());
        for (Map.Entry<String, String> entry : this.scoreMap.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeString(entry.getValue());
        }
        dest.writeInt(this.scoreEarnedTotal);
        dest.writeFloat(this.evalSumTotal);
        dest.writeFloat(this.evalAverageTotal);
        dest.writeFloat(this.evalPercentTotal);
        dest.writeTypedList(this.semesterScores);
    }

    public WiseScores() {
    }

    protected WiseScores(Parcel in) {
        int scoreMapSize = in.readInt();
        this.scoreMap = new HashMap<String, String>(scoreMapSize);
        for (int i = 0; i < scoreMapSize; i++) {
            String key = in.readString();
            String value = in.readString();
            this.scoreMap.put(key, value);
        }
        this.scoreEarnedTotal = in.readInt();
        this.evalSumTotal = in.readFloat();
        this.evalAverageTotal = in.readFloat();
        this.evalPercentTotal = in.readFloat();
        this.semesterScores = in.createTypedArrayList(SemesterScore.CREATOR);
    }

    public static final Parcelable.Creator<WiseScores> CREATOR = new Parcelable.Creator<WiseScores>() {
        @Override
        public WiseScores createFromParcel(Parcel source) {
            return new WiseScores(source);
        }

        @Override
        public WiseScores[] newArray(int size) {
            return new WiseScores[size];
        }
    };
}
