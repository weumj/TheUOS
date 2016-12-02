package com.uoscs09.theuos2.tab.timetable;


import com.uoscs09.theuos2.tab.subject.Subject;

import java.util.ArrayList;

import mj.android.utils.xml.Element;
import mj.android.utils.xml.Root;

@Root(name = "list")
public class SimpleSubject {

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

    //todo refactor
    public Subject toSubject(Timetable2 timeTable, Timetable2.SubjectInfo subject) {
        Subject item = new Subject();
        item.subject_no = subjectNo;
        item.subject_nm = subjectName;
        item.subject_div = subjectDiv;
        item.class_div = classDiv;
        item.credit = credit;
        item.sub_dept = dept;
        item.prof_nm = professorName;

        item.term = timeTable.semester().code;
        item.year = Integer.toString(timeTable.year());

        ArrayList<Subject.ClassInformation> classInformationList = timeTable.classTimeInformationTable().get(subject.nameKor());
        if (classInformationList != null)
            item.classInformation().addAll(classInformationList);

        item.setInfoArray();

        return item;

    }

}
