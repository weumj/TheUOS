package com.uoscs09.theuos2.util;

import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;

import com.uoscs09.theuos2.R;

public class ResourceUtil {
    /**
     * 현재 테마에 정의된, {@link R.attr}에 선언된 값을 가져온다.
     *
     * @param attrId 가져올 값의 Id
     * @return 현재 테마에서 정의한 해당 값의 id
     */
    public static int getAttrValue(Context context, @AttrRes int attrId) {
        TypedValue out = new TypedValue();
        context.getTheme().resolveAttribute(attrId, out, true);

        return out.resourceId;
    }

    @ColorInt
    public static int getAttrColor(Context context, @AttrRes int attrColorId) {
        return ContextCompat.getColor(context, getAttrValue(context, attrColorId));
    }

    @ColorInt
    public static int getOrderedColor(Context context, int index) {
        return ContextCompat.getColor(context, getOrderedColorRes(index));
    }

    @ColorRes
    public static int getOrderedColorRes(int index) {
        switch (index % 17) {
            case 0:
            default:
                return R.color.red_yellow;
            case 1:
                return R.color.light_blue;
            case 2:
                return R.color.material_light_blue_700;
            case 3:
                return R.color.purple;
            case 4:
                return R.color.green;
            case 5:
                return R.color.gray_blue;
            case 6:
                return R.color.material_blue_grey_400;
            case 7:
                return R.color.material_green_700;
            case 8:
                return R.color.material_deep_teal_500_1;
            case 9:
                return R.color.material_blue_grey_200;
            case 10:
                return R.color.material_deep_teal_200_1;
            case 11:
                return R.color.material_grey_600_;
            case 12:
                return R.color.material_red_200;
            case 13:
                return R.color.material_light_blue_200;
            case 14:
                return R.color.material_indigo_200;
            case 15:
                return R.color.material_green_200;
            case 16:
                return R.color.material_green_300;
        }
    }

    /**
     * 화면 크기를 판단한다.
     *
     * @return {@code true} - 화면 크기가
     * {@link Configuration#SCREENLAYOUT_SIZE_NORMAL} 이하 일 경우<br>
     * <br>
     * {@code false} - 그 외
     */
    public static boolean isScreenSizeSmall() {
        int sizeInfoMasked = AppUtil.context().getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

        switch (sizeInfoMasked) {
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                return true;
            default:
                return false;
        }
    }
}
