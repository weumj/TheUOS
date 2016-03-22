package com.uoscs09.theuos2.tab.subject;

import com.uoscs09.theuos2.parse.IParser;

import java.util.List;

import mj.android.utils.xml.ListContainer;
import mj.android.utils.xml.Root;

@Root(name = "root", charset = "euc-kr")
public class TimeTableSubjectInfo implements IParser.IPostParsing {
    @ListContainer(name = "mainlist")
    private List<SubjectItem2> subjectInfoItems;

    public List<SubjectItem2> subjectInfoList() {
        return subjectInfoItems;
    }

    @Override
    public void afterParsing() {
        if (subjectInfoItems != null)
            for (SubjectItem2 item2 : subjectInfoItems)
                item2.afterParsing();
    }
}
