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
    public String getListTag() {
        return "mainlist";
    }
}
