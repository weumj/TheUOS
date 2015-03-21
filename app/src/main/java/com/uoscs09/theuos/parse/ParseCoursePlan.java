package com.uoscs09.theuos.parse;

import android.support.annotation.NonNull;

import com.uoscs09.theuos.tab.subject.CoursePlanItem;

public class ParseCoursePlan extends OApiParser2.ReflectionParser<CoursePlanItem> {
    @NonNull
    @Override
    protected CoursePlanItem newInstance() {
        return new CoursePlanItem();
    }

    @NonNull
    @Override
    protected Class<? extends CoursePlanItem> getReflectionClass(CoursePlanItem instance) {
        return instance.getClass();
    }

    @NonNull
    @Override
    public String getListTag() {
        return "mainlist";
    }
}
