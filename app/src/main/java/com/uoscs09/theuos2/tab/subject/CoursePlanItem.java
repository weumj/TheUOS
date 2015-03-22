package com.uoscs09.theuos2.tab.subject;

import android.os.Parcel;
import android.os.Parcelable;

import com.uoscs09.theuos2.annotation.KeepName;
import com.uoscs09.theuos2.util.StringUtil;
@KeepName
public class CoursePlanItem implements Parcelable {

    public String subject_no = StringUtil.NULL;
    public String subject_nm = StringUtil.NULL;
    public String prof_nm= StringUtil.NULL;
    public String tel_no= StringUtil.NULL;
    public String score_eval_rate= StringUtil.NULL;
    public String book_nm= StringUtil.NULL;

    public int week;
    public String week_title= StringUtil.NULL;
    public String class_cont= StringUtil.NULL;
    public String class_meth= StringUtil.NULL;
    public String week_book= StringUtil.NULL;
    public String prjt_etc= StringUtil.NULL;

    private CoursePlanItem(Parcel source) {
        subject_no = source.readString();
        subject_nm = source.readString();
        prof_nm = source.readString();
        tel_no  = source.readString();
        score_eval_rate  = source.readString();
        book_nm  = source.readString();

        week = source.readInt();
        week_title = source.readString();
        class_cont  = source.readString();
        class_meth  = source.readString();
        week_book = source.readString();
        prjt_etc = source.readString();
    }

    public CoursePlanItem(){}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(subject_no);
        dest.writeString(subject_nm);
        dest.writeString(prof_nm);
        dest.writeString(tel_no);
        dest.writeString(score_eval_rate);
        dest.writeString(book_nm);

        dest.writeInt(week);
        dest.writeString(week_title);
        dest.writeString(class_cont);
        dest.writeString(class_meth);
        dest.writeString(week_book);
        dest.writeString(prjt_etc);
    }

    public static final Creator<CoursePlanItem> CREATOR = new Creator<CoursePlanItem>() {

        @Override
        public CoursePlanItem createFromParcel(Parcel source) {
            return new CoursePlanItem(source);
        }

        @Override
        public CoursePlanItem[] newArray(int size) {
            return new CoursePlanItem[size];
        }

    };
}
