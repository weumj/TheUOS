package com.uoscs09.theuos2.tab.schedule;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.CalendarContract.Events;
import android.support.annotation.Keep;

import com.uoscs09.theuos2.parse.IParser;

import java.io.Serializable;
import java.util.Calendar;

import mj.android.utils.xml.Element;
import mj.android.utils.xml.Root;

@Keep
@Root(name = "list")
public class UnivScheduleItem implements Serializable, Parcelable, IParser.IPostParsing {
    private static final long serialVersionUID = 4167246292492938182L;

    @Element(name = "content", cdata = true)
    public String content;
    @Element(name = "sch_date", cdata = true)
    public String scheduleDate;
    @Element(name = "year", cdata = true)
    private String year;
    @Element(name = "month", cdata = true)
    private String month;

    /**
     * ex) 07.01 (월)
     */
    public ScheduleDate dateStart = ScheduleDate.EMPTY, dateEnd = ScheduleDate.EMPTY;

    public UnivScheduleItem() {
    }

    @Override
    public void afterParsing() {
        parseDate();
    }

    private void parseDate() {
        String[] array = scheduleDate.split("~");

        if (array.length > 1) {
            dateStart = new ScheduleDate(array[0]);
            dateEnd = new ScheduleDate(array[1]);

        } else if (scheduleDate.contains(" (")) {
            dateStart = dateEnd = new ScheduleDate(scheduleDate);
        }
    }

    public Calendar getDate(boolean isStart) {
        try {
            int y = Integer.parseInt(year.substring(0, 4));
            return isStart ? dateStart.getDate(y, true) : dateEnd.getDate(y, false);
        } catch (Exception e) {
            return null;
        }

    }

    public ContentValues toContentValues(long id) {
        ContentValues cv = new ContentValues();
        cv.put(Events.CALENDAR_ID, id);
        cv.put(Events.TITLE, content);
        cv.put(Events.DESCRIPTION, content);

        cv.put(Events.DTSTART, getDate(true).getTimeInMillis());
        cv.put(Events.DTEND, getDate(false).getTimeInMillis());
        cv.put(Events.EVENT_TIMEZONE, "Asia/Seoul");

        return cv;
    }

    private UnivScheduleItem(Parcel source) {
        content = source.readString();
        scheduleDate = source.readString();
        year = source.readString();
        month = source.readString();
        dateStart = source.readParcelable(ScheduleDate.class.getClassLoader());
        dateEnd = source.readParcelable(ScheduleDate.class.getClassLoader());
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(content);
        dest.writeString(scheduleDate);
        dest.writeString(year);
        dest.writeString(month);
        dest.writeParcelable(dateStart, flags);
        dest.writeParcelable(dateEnd, flags);
    }

    public static final Creator<UnivScheduleItem> CREATOR = new Creator<UnivScheduleItem>() {

        @Override
        public UnivScheduleItem createFromParcel(Parcel source) {
            return new UnivScheduleItem(source);
        }

        @Override
        public UnivScheduleItem[] newArray(int size) {
            return new UnivScheduleItem[size];
        }

    };

    public static class ScheduleDate implements Serializable, Parcelable {
        private static final long serialVersionUID = 8835161151926579336L;
        public int month = -1;
        public int day = -1;
        public int dayInWeek = -1;

        ScheduleDate() {
        }

        ScheduleDate(String string) {
            String[] array = string.split(" ");

            if (array.length > 1) {
                switch (array[1].trim()) {
                    case "(일)":
                        dayInWeek = 0;
                        break;

                    case "(월)":
                        dayInWeek = 1;
                        break;

                    case "(화)":
                        dayInWeek = 2;
                        break;

                    case "(수)":
                        dayInWeek = 3;
                        break;

                    case "(목)":
                        dayInWeek = 4;
                        break;

                    case "(금)":
                        dayInWeek = 5;
                        break;

                    case "(토)":
                        dayInWeek = 6;
                        break;

                    default:
                        break;
                }

                array = array[0].split("\\.");

                if (array.length > 1) {
                    month = Integer.parseInt(array[0]);
                    day = Integer.parseInt(array[1]);
                }

            }
        }

        public static final ScheduleDate EMPTY;

        static {
            EMPTY = new ScheduleDate();
            EMPTY.dayInWeek = EMPTY.day = EMPTY.month = -1;
        }


        public boolean isEmpty() {
            return dayInWeek == day && day == month && month == -1;
        }


        Calendar getDate(int year, boolean isStart) {
            Calendar c = Calendar.getInstance();
            c.set(year, month - 1, day, isStart ? 9 : 18, 0);

            return c;
        }

        ScheduleDate(Parcel source) {
            month = source.readInt();
            day = source.readInt();
            dayInWeek = source.readInt();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(month);
            dest.writeInt(day);
            dest.writeInt(dayInWeek);
        }

        public static final Creator<ScheduleDate> CREATOR = new Creator<ScheduleDate>() {

            @Override
            public ScheduleDate createFromParcel(Parcel source) {
                return new ScheduleDate(source);
            }

            @Override
            public ScheduleDate[] newArray(int size) {
                return new ScheduleDate[size];
            }

        };
    }
}
