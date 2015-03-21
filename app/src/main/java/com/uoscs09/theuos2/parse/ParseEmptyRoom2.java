package com.uoscs09.theuos2.parse;


import android.support.annotation.NonNull;

import com.uoscs09.theuos2.tab.emptyroom.ClassRoomItem;

public class ParseEmptyRoom2 extends OApiParser2.ReflectionParser<ClassRoomItem>{
    @NonNull
    @Override
    protected ClassRoomItem newInstance() {
        return new ClassRoomItem();
    }

    @NonNull
    @Override
    protected Class<? extends ClassRoomItem> getReflectionClass(ClassRoomItem instance) {
        return instance.getClass();
    }

    @NonNull
    @Override
    public String getListTag() {
        return "mainlist";
    }
}
