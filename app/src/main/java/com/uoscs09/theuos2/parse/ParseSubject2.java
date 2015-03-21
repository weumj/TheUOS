package com.uoscs09.theuos2.parse;


import android.support.annotation.NonNull;

import com.uoscs09.theuos2.tab.subject.SubjectItem2;

public class ParseSubject2 extends OApiParser2.ReflectionParser<SubjectItem2>{
    @NonNull
    @Override
    protected SubjectItem2 newInstance() {
        return new SubjectItem2();
    }

    @NonNull
    @Override
    protected Class<? extends SubjectItem2> getReflectionClass(SubjectItem2 instance) {
        return instance.getClass();
    }

    @Override
    protected void afterParseList(SubjectItem2 item) {
        super.afterParseList(item);
        item.setInfoArray();
    }

    @NonNull
    @Override
    public String getListTag() {
        return "mainlist";
    }
}
