package com.uoscs09.theuos2.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.uoscs09.theuos2.tab.announce.AnnounceItem;

import java.util.List;

import mj.android.utils.task.Task;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface AnnounceApi {
    String URL_M_SCHOLARSHIP = "http://m.uos.ac.kr/mkor/schBoard/list.do";
    String URL_M_ANNOUNCE = "http://m.uos.ac.kr/mkor/notBoard/list.do";

    @FormUrlEncoded
    @POST
    Task<List<AnnounceItem>> announces(
            @NonNull @Url String url,
            @NonNull @Field("list_id") String category,
            @Field("pageIndex") int page,
            @Nullable @Field("searchCnd") String searchCondition,
            @Nullable @Field("searchWrd") String searchWord
    );

    @FormUrlEncoded
    @POST
    Task<List<AnnounceItem>> scholarships(
            @NonNull @Url String url,
            @Field("pageIndex") int page,
            @Field("brdBbsseq") int brdBbsseq,
            @Nullable @Field("skind") String searchKind,
            @Nullable @Field("sword") String searchWord
    );

}
