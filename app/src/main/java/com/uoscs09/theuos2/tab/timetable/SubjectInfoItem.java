package com.uoscs09.theuos2.tab.timetable;


import com.uoscs09.theuos2.tab.subject.SubjectItem2;

import java.util.ArrayList;

import mj.android.utils.xml.Element;
import mj.android.utils.xml.Root;

@Root(name = "list")
public class SubjectInfoItem {

    @Element(name = "subject_no")
    public String subjectNo;
    @Element(name = "subject_nm")
    public String subjectName;
    @Element(name = "class_div")
    public String classDiv;
    @Element(name = "subject_div")
    public String subjectDiv;
    @Element(name = "credit")
    public int credit;
    @Element(name = "dept")
    public String dept;
    @Element(name = "prof_nm")
    public String professorName;

    public SubjectItem2 toSubjectItem(TimeTable timeTable, Subject subject) {
        SubjectItem2 item = new SubjectItem2();
        item.subject_no = subjectNo;
        item.subject_nm = subjectName;
        item.subject_div = subjectDiv;
        item.class_div = classDiv;
        item.credit = credit;
        item.sub_dept = dept;
        item.prof_nm = professorName;

        item.term = timeTable.semesterCode.code;
        item.year = Integer.toString(timeTable.year);

        ArrayList<SubjectItem2.ClassInformation> classInformationList = timeTable.getClassTimeInformationTable().get(subject.subjectName);
        if (classInformationList != null)
            item.classInformation().addAll(classInformationList);

        item.setInfoArray();

        return item;

    }

}
