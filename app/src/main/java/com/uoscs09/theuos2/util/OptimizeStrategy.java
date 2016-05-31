package com.uoscs09.theuos2.util;

import android.content.Context;
import android.os.BatteryManager;
import android.os.Build;

public class OptimizeStrategy {

    public static boolean isSafeToOptimize() {
        return isCoreSizeAbove2() && isBatteryPercentageAbove29(AppUtil.context());
    }

    private static boolean isBatteryPercentageAbove29(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BatteryManager batteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
            return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) > 29;
        } else {
            return true;
        }
    }

    private static boolean isCoreSizeAbove2() {
        return Runtime.getRuntime().availableProcessors() > 2;
    }
}
