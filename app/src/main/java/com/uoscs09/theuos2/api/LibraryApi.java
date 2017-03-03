package com.uoscs09.theuos2.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.uoscs09.theuos2.tab.booksearch.BookDetailItem;
import com.uoscs09.theuos2.tab.booksearch.BookItem;
import com.uoscs09.theuos2.tab.booksearch.BookStateWrapper;
import com.uoscs09.theuos2.tab.libraryseat.SeatTotalInfo;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;
import retrofit2.http.Url;
import rx.Observable;

public interface LibraryApi {
    String BOOK_URL = "http://mlibrary.uos.ac.kr/search/tot/";
    String SEATS_URL = "http://203.249.102.34:8080/seat/domian5.asp";

    @Headers({"Content-Type: charset=EUC-KR"})
    @GET("result?sm=&st=KWRD&websysdiv=tot&si=TOTAL&websysdiv=tot")
    Observable<List<BookItem>> books(
            @Query("pn") int page,
            @NonNull @Query("q") String query
    );

    @Headers({"Content-Type: charset=EUC-KR"})
    @GET("result?sm=&st=KWRD&websysdiv=tot&si=TOTAL")
    Observable<List<BookItem>> books(
            @Query("pn") int page,
            @NonNull @Query("q") String query,
            @Nullable @Query("oi") String optionIndex,
            @Nullable @Query("os") String optionSort
    );

    //@Headers({"Content-Type: charset=EUC-KR"})
    @GET
    Observable<BookStateWrapper> bookStateInformation(
            @Url String url
    );

    //fixme
    @Headers({"Content-Type: text/html; charset=EUC-KR"})
    @GET
    Observable<BookDetailItem> bookDetailItem(
            @Url String url
    );

    @Headers({"Content-Type: text/html; charset=EUC-KR"})
    @GET
    Observable<SeatTotalInfo> seatInformation(
            @Url String url
    );
}
