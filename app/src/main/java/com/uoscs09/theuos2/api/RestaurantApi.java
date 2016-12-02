package com.uoscs09.theuos2.api;

import android.support.annotation.NonNull;
import android.util.SparseArray;

import com.uoscs09.theuos2.tab.restaurant.RestItem;
import com.uoscs09.theuos2.tab.restaurant.RestWeekItem;

import mj.android.utils.task.Task;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface RestaurantApi {
    String URL = "http://m.uos.ac.kr/mkor/food/";
    String WEEK_URL = "http://www.uos.ac.kr/food/placeList.do";

    @Headers({"Content-Type: text/html; charset=EUC-KR"})
    @GET("list.do")
    Task<SparseArray<RestItem>> restItem(
    );

    @Headers({"Content-Type: text/html; charset=EUC-KR"})
    @GET
    Task<RestWeekItem> weekRestItem(
            @NonNull @Url String url,
            @NonNull @Query("rstcde") String category
    );
}
