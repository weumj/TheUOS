package com.uoscs09.theuos2.tab.timetable;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.RequiresPermission;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.util.IOUtil;
import com.uoscs09.theuos2.util.ImageUtil;
import com.uoscs09.theuos2.util.PrefHelper;
import com.uoscs09.theuos2.util.PrefUtil;

import mj.android.utils.task.Task;

public class TimetableUtil {

    //*********** timetable ***********

    public static boolean deleteTimetable(Context context) {
        boolean b = context.deleteFile(IOUtil.FILE_TIMETABLE);

        clearTimeTableColor(context);
        //TimetableAlarmUtil.clearAllAlarm(context);

        return b;
    }


    //************ color *************

    public static void putTimeTableColor(Context context, int idx, int color) {
        if (idx > -1 && idx < 10) {
            PrefUtil.getInstance(context).put("color" + idx, color);
        }
    }

    public static int getTimeTableColor(Context context, int idx) {
        if (idx > -1 && idx < 10) {
            return PrefUtil.getInstance(context).get("color" + idx, ContextCompat.getColor(context, getTimeTableColorDefaultResource(idx)));
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
                return R.color.material_deep_teal_500_1;
            default:
                return 0;
        }
    }


    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public static Task<String> saveTimetableToImage(Timetable2 timetable, ListView listView, ListAdapter originalAdapter, View header) {
        //noinspection ResourceType
        final String picturePath = PrefHelper.Data.getPicturePath();
        @SuppressLint("DefaultLocale")
        String savedPath = String.format("%s/timetable_%d_%s_%d.png", picturePath, timetable.year(), timetable.semester().name(), System.currentTimeMillis());

        return new ImageUtil.ListViewBitmapRequest.Builder(listView, originalAdapter)
                .setHeaderView(header)
                .build()
                .map(new ImageUtil.ImageWriteProcessor(savedPath));

    }
}
