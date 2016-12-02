package com.uoscs09.theuos2.tab.subject;

import android.os.Parcel;
import android.os.Parcelable;

import mj.android.utils.xml.Element;
import mj.android.utils.xml.Root;

@Root(name = "list")
public class CoursePlan implements Parcelable {

    @Element(name = "subject_no", cdata = true)
    public String subject_no;
    @Element(name = "subject_nm", cdata = true)
    public String subject_nm;
    @Element(name = "prof_nm", cdata = true)
    public String prof_nm;
    @Element(name = "tel_no", cdata = true)
    public String tel_no;
    @Element(name = "score_eval_rate", cdata = true)
    public String score_eval_rate;
    @Element(name = "book_nm", cdata = true)
    public String book_nm;
    @Element(name = "term", cdata = true)
    public String term;

    @Element(name = "week")
    public int week;
    @Element(name = "week_title", cdata = true)
    public String week_title;
    @Element(name = "class_cont", cdata = true)
    public String class_cont;
    @Element(name = "class_meth", cdata = true)
    public String class_meth;
    @Element(name = "week_book", cdata = true)
    public String week_book;
    @Element(name = "prjt_etc", cdata = true)
    public String prjt_etc;

    private CoursePlan(Parcel source) {
        subject_no = source.readString();
        subject_nm = source.readString();
        prof_nm = source.readString();
        tel_no = source.readString();
        score_eval_rate = source.readString();
        book_nm = source.readString();
        term = source.readString();

        week = source.readInt();
        week_title = source.readString();
        class_cont = source.readString();
        class_meth = source.readString();
        week_book = source.readString();
        prjt_etc = source.readString();
    }

    public CoursePlan() {
    }

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
        dest.writeString(term);

        dest.writeInt(week);
        dest.writeString(week_title);
        dest.writeString(class_cont);
        dest.writeString(class_meth);
        dest.writeString(week_book);
        dest.writeString(prjt_etc);
    }

    public static final Creator<CoursePlan> CREATOR = new Creator<CoursePlan>() {

        @Override
        public CoursePlan createFromParcel(Parcel source) {
            return new CoursePlan(source);
        }

        @Override
        public CoursePlan[] newArray(int size) {
            return new CoursePlan[size];
        }

    };
}
