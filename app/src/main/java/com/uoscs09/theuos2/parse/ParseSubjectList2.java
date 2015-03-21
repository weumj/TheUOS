package com.uoscs09.theuos2.parse;

import android.support.annotation.NonNull;

import com.uoscs09.theuos2.tab.timetable.SubjectInfoItem;

public class ParseSubjectList2 extends OApiParser2.ReflectionParser<SubjectInfoItem>{
    @NonNull
    @Override
    public String getListTag() {
        return "mainlist";
    }


    @NonNull
    @Override
    protected SubjectInfoItem newInstance() {
        return new SubjectInfoItem();
    }

    @NonNull
    @Override
    protected Class<? extends SubjectInfoItem> getReflectionClass(SubjectInfoItem instance) {
        return instance.getClass();
    }
}
