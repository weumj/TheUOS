package com.uoscs09.theuos.tab.timetable;


import com.uoscs09.theuos.tab.subject.SubjectItem;

public class SubjectInfoItem {

    public String subject_no;
    public String subject_nm;
    public String class_div;
    public String subject_div;
    public int credit;
    public String dept;
    public String prof_nm;

    public SubjectItem toSubjectItem() {
        SubjectItem item = new SubjectItem();
        item.infoArray[0] = dept;
        item.infoArray[1] = subject_div;
        item.infoArray[4] = class_div;
        item.infoArray[3] = subject_no;
        item.infoArray[5] = subject_nm;
        item.infoArray[7] = Integer.toString(credit);
        item.infoArray[8] = prof_nm;

        return item;
    }
}
