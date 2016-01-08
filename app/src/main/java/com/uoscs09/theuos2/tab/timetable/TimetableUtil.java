package com.uoscs09.theuos2.tab.timetable;


import android.content.Context;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.async.Request;
import com.uoscs09.theuos2.common.SerializableArrayMap;
import com.uoscs09.theuos2.util.IOUtil;
import com.uoscs09.theuos2.util.ImageUtil;
import com.uoscs09.theuos2.util.PrefUtil;
import com.uoscs09.theuos2.util.StringUtil;

import java.io.IOException;
import java.util.ArrayList;

public class TimetableUtil {

    //*********** timetable ***********

    public static boolean deleteTimetable(Context context) {
        boolean b = context.deleteFile(IOUtil.FILE_TIMETABLE);

        clearTimeTableColor(context);
        //TimetableAlarmUtil.clearAllAlarm(context);

        return b;
    }

    /**
     * 시간표 정보를 파일로부터 읽어온다.
     *
     * @return 시간표 정보 파일이 없다면 null
     */
    @Nullable
    public static TimeTable readTimetable(Context context) {
        return IOUtil.readFromFileSuppressed(context, IOUtil.FILE_TIMETABLE);
    }

    public static void writeTimetable(Context context, TimeTable timeTable) throws IOException {
        IOUtil.writeObjectToFile(context, IOUtil.FILE_TIMETABLE, timeTable);
    }


    /**
     * 주어진 시간표정보를 통해 시간표 각 과목과 컬러를 mapping하는 Map을 작성한다.
     * -- 과목이름이 Key 이고, Value 가 컬러를 가리키는 Integer 인 Map<br>
     * 컬러는 단순한 정수이며, AppUtil 을 통해 Color integer 를 얻어와야 한다.
     *
     * @param timetable 시간표
     */
    public static void makeColorTable(TimeTable timetable) {
        SerializableArrayMap<String, Integer> table = new SerializableArrayMap<>();

        ArrayList<Subject[]> subjects = timetable.subjects;

        String subjectName;
        int i = 0;
        for (Subject[] subjectArray : subjects) {
            for (Subject subject : subjectArray) {

                if (subject.equals(Subject.EMPTY))
                    continue;

                subjectName = subject.subjectName;
                if (!subjectName.equals(StringUtil.NULL) && !table.containsKey(subjectName)) {
                    table.put(subjectName, i++);
                }
            }
        }

        timetable.setColorTable(table);
    }

    //************ color *************

    public static void putTimeTableColor(Context context, int idx, int color) {
        if (idx > -1 && idx < 10) {
            PrefUtil.getInstance(context).put("color" + idx, color);
        }
    }

    public static int getTimeTableColor(Context context, int idx) {
        if (idx > -1 && idx < 10) {
            return PrefUtil.getInstance(context).get("color" + idx, context.getResources().getColor(getTimeTableColorDefaultResource(idx)));
        }

        return 0;
    }

    public static void clearTimeTableColor(Context context) {
        String[] array = new String[10];
        for (int i = 0; i < 10; i++)
            array[i] = "color" + i;

        PrefUtil.getInstance(context).remove(array);
    }

    public static int getTimeTableColorDefaultResource(int idx) {
        switch (idx) {
            case 0:
                return R.color.red_yellow;
            case 1:
                return R.color.light_blue;
            case 2:
                return R.color.red_material_300;
            case 3:
                return R.color.purple;
            case 4:
                return R.color.green;
            case 5:
                return R.color.gray_blue;
            case 6:
                return R.color.material_blue_grey_400;
            case 7:
                return R.color.pink;
            case 8:
                return R.color.material_green_700;
            case 9:
                return R.color.material_deep_teal_500;
            default:
                return 0;
        }
    }


    public static Request<String> saveTimetableToImage(TimeTable timeTable, ListView listView, ListAdapter originalAdapter, View header) {
        final String picturePath = PrefUtil.getPicturePath(listView.getContext());
        String savedPath = picturePath + "/timetable_" + timeTable.year + '_' + timeTable.semesterCode + '_' + String.valueOf(System.currentTimeMillis()) + ".png";

        return new ImageUtil.ListViewBitmapRequest.Builder(listView, originalAdapter)
                .setHeaderView(header)
                .build()
                .wrap(new ImageUtil.ImageWriteProcessor(savedPath));

    }
}
