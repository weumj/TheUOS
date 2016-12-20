package com.uoscs09.theuos2.tab.timetable;


import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.util.PrefUtil;

public class TimetableUtil {
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
}
