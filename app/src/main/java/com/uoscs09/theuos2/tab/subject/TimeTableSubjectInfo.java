package com.uoscs09.theuos2.tab.subject;

import android.support.annotation.Keep;

import com.uoscs09.theuos2.parse.IParser;

import java.util.List;

import mj.android.utils.xml.ListContainer;
import mj.android.utils.xml.Root;

@Keep
@Root(name = "root", charset = "euc-kr")
public class TimeTableSubjectInfo implements IParser.IPostParsing {
    @ListContainer(name = "mainlist")
    private List<Subject> subjectInfoItems;

    public List<Subject> subjectInfoList() {
        return subjectInfoItems;
    }

    @Override
    public void afterParsing() {
        if (subjectInfoItems != null)
            for (Subject item2 : subjectInfoItems)
                item2.afterParsing();
    }
}
