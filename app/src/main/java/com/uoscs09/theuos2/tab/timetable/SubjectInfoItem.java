package com.uoscs09.theuos2.tab.timetable;


import com.uoscs09.theuos2.annotation.KeepName;
import com.uoscs09.theuos2.tab.subject.SubjectItem2;
@KeepName
public class SubjectInfoItem {

    public String subject_no;
    public String subject_nm;
    public String class_div;
    public String subject_div;
    public int credit;
    public String dept;
    public String prof_nm;

    public SubjectItem2 toSubjectItem(TimeTable timeTable) {
        SubjectItem2 item = new SubjectItem2();
        item.subject_no = subject_no;
        item.subject_nm = subject_nm;
        item.subject_div = subject_div;
        item.class_div = class_div;
        item.credit = credit;
        item.sub_dept = dept;
        item.prof_nm = prof_nm;
        item.term = timeTable.semesterCode.code;
        item.year = Integer.toString(timeTable.year);
        item.setInfoArray();

        return item;

    }

}
