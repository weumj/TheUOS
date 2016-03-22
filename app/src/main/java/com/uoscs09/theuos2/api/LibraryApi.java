package com.uoscs09.theuos2.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.uoscs09.theuos2.tab.booksearch.BookItem;
import com.uoscs09.theuos2.tab.booksearch.BookStates;
import com.uoscs09.theuos2.tab.libraryseat.SeatInfo;

import java.util.List;

import mj.android.utils.task.Task;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface LibraryApi {
    String BOOK_URL = "http://mlibrary.uos.ac.kr/search/tot/";
    String SEATS_URL = "http://203.249.102.34:8080/seat/domian5.asp";

    @FormUrlEncoded
    @Headers({"Content-Type: charset=EUC-KR"})
    @POST("result?sm=&st=KWRD&websysdiv=tot&si=TOTAL&websysdiv=tot")
    Task<List<BookItem>> books(
            @Field("pn") int page,
            @NonNull @Field("q") String query
    );

    @FormUrlEncoded
    @Headers({"Content-Type: charset=EUC-KR"})
    @POST("result?sm=&st=KWRD&websysdiv=tot&si=TOTAL")
    Task<List<BookItem>> books(
            @Field("pn") int page,
            @NonNull @Field("q") String query,
            @Nullable @Field("oi") String optionIndex,
            @Nullable @Field("os") String optionSort
    );

    //@Headers({"Content-Type: charset=EUC-KR"})
    @GET
    Task<BookStates> bookStateInformation(
            @Url String url
    );

    @Headers({"Content-Type: charset=EUC-KR"})
    @GET
    Task<SeatInfo> seatInformation(
            @Url String url
    );
}
