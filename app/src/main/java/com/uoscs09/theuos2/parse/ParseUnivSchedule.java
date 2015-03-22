package com.uoscs09.theuos2.parse;


import android.support.annotation.NonNull;

import com.uoscs09.theuos2.tab.schedule.UnivScheduleItem;

public class ParseUnivSchedule extends OApiParser2.ReflectionParser<UnivScheduleItem> {
    @NonNull
    @Override
    protected UnivScheduleItem newInstance() {
        return new UnivScheduleItem();
    }

    @NonNull
    @Override
    public String getListTag() {
        return "schList";
    }

}
