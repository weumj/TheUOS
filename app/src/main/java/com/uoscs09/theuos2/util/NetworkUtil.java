package com.uoscs09.theuos2.util;

import mj.android.utils.common.NetworkUtils;

public class NetworkUtil {
    public static boolean isConnectivityEnable(){
        return NetworkUtils.isConnectivityEnable(AppUtil.context);
    }

}
