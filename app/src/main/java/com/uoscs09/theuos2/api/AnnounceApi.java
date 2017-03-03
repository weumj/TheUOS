package com.uoscs09.theuos2.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.uoscs09.theuos2.tab.announce.AnnounceItem;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Url;
import rx.Observable;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

public interface AnnounceApi {
    @Target(METHOD)
    @Retention(RUNTIME)
    @interface Mobile {
    }

    @Target(METHOD)
    @Retention(RUNTIME)
    @interface Scholarship {
    }

    String URL_M_SCHOLARSHIP = "http://m.uos.ac.kr/mkor/schBoard/list.do";
    String URL_M_ANNOUNCE = "http://m.uos.ac.kr/mkor/notBoard/list.do";

    String URL_SCHOLARSHIP = "http://scholarship.uos.ac.kr/scholarship/notice/notice/list.do";

    @FormUrlEncoded
    @POST("korNotice/list.do")
    Observable<List<AnnounceItem>> announces(
            @NonNull @Field("list_id") String category,
            @Field("pageIndex") int page,
            @Nullable @Field("searchCnd") String searchCondition,
            @Nullable @Field("searchWrd") String searchWord
    );

    @FormUrlEncoded
    @POST
    @Scholarship
    Observable<List<AnnounceItem>> scholarships(
            @NonNull @Url String url,
            @Field("pageIndex") int page,
            @Field("brdBbsseq") int brdBbsseq,
            @Nullable @Field("skind") String searchKind,
            @Nullable @Field("sword") String searchWord
    );

    @FormUrlEncoded
    @POST
    @Mobile
    Observable<List<AnnounceItem>> announcesMobile(
            @NonNull @Url String url,
            @NonNull @Field("list_id") String category,
            @Field("pageIndex") int page,
            @Nullable @Field("searchCnd") String searchCondition,
            @Nullable @Field("searchWrd") String searchWord
    );

    // search x
    @FormUrlEncoded
    @POST
    @Mobile
    @Scholarship
    Observable<List<AnnounceItem>> scholarshipsMobile(
            @NonNull @Url String url,
            @Field("pageIndex") int page,
            @Field("brdBbsseq") int brdBbsseq
    );

}
