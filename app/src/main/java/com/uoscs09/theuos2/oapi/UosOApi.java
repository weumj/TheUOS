package com.uoscs09.theuos2.oapi;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.uoscs09.theuos2.tab.emptyroom.EmptyRoom;
import com.uoscs09.theuos2.tab.schedule.UnivScheduleItem;
import com.uoscs09.theuos2.tab.subject.CoursePlanItem;
import com.uoscs09.theuos2.tab.subject.SubjectItem2;
import com.uoscs09.theuos2.tab.timetable.SubjectInfoItem;
import com.uoscs09.theuos2.util.OApiUtil;

import java.util.List;

import mj.android.utils.task.Task;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface UosOApi {
    String URL = "http://wise.uos.ac.kr/uosdoc/";

    String key = OApiUtil.UOS_API_KEY;

    @FormUrlEncoded
    @Headers({"Content-Type: charset=EUC-KR"})
    @POST("api.ApiUcsFromToEmptyRoom.oapi")
    Task<List<EmptyRoom>> emptyRooms(
            @NonNull @Field("apiKey") String apiKey,
            @NonNull @Field("year") String year,
            @NonNull @Field("term") String term,
            @NonNull @Field("building") String building,
            @NonNull @Field("dateFrom") String dateFrom,
            @NonNull @Field("dateTo") String dateTo,
            @NonNull @Field("wdayTime") String wdayTime,
            @Nullable @Field("classRoomDiv") String classRoomDiv,
            @Nullable @Field("aplyPosbYn") String aplyPosbYn
    );

    @FormUrlEncoded
    @Headers({"Content-Type: charset=EUC-KR"})
    @POST("api.ApiApiCoursePlanView.oapi")
    Task<List<CoursePlanItem>> coursePlans(
            @NonNull @Field("apiKey") String apiKey,
            @NonNull @Field("term") String term,
            @NonNull @Field("subjectNo") String subjectNo,
            @NonNull @Field("classDiv") String classDiv,
            @NonNull @Field("year") String year
    );

    @FormUrlEncoded
    @Headers({"Content-Type: charset=EUC-KR"})
    @POST("api.ApiUcrCultTimeInq.oapi")
    Task<List<SubjectItem2>> timetableCulture(
            @NonNull @Field("apiKey") String apiKey,
            @NonNull @Field("year") String year,
            @NonNull @Field("term") String term,
            @NonNull @Field("subjectDiv") String subjectDiv,
            @Nullable @Field("subjectSubDiv") String subjectSubDiv,
            @Nullable @Field("subjectNm") String subjectNm
    );

    @FormUrlEncoded
    @Headers({"Content-Type: charset=EUC-KR"})
    @POST("api.ApiUcrMjTimeInq.oapi")
    Task<List<SubjectItem2>> timetableMajor(
            @NonNull @Field("apiKey") String apiKey,
            @NonNull @Field("year") String year,
            @NonNull @Field("term") String term,
            @NonNull @Field("deptDiv") String deptDiv,
            @NonNull @Field("dept") String dept,
            @NonNull @Field("subDept") String subDept,
            @Nullable @Field("subjectDiv") String subjectDiv,
            @Nullable @Field("subjectNo") String subjectNo,
            @Nullable @Field("classDiv") String classDiv,
            @Nullable @Field("subjectNm") String subjectNm,
            @Nullable @Field("etcExc") String bEtcExcludeYN
    );


    @FormUrlEncoded
    @Headers({"Content-Type: charset=EUC-KR"})
    @POST("api.ApiApiSubjectList.oapi")
    Task<List<SubjectInfoItem>> subjectInformation(
            @NonNull @Field("apiKey") String apiKey,
            @NonNull @Field("year") String year,
            @NonNull @Field("term") String term,
            @NonNull @Field("subjectNm") String subjectNm,
            @Nullable @Field("subjectNo") String subjectNo,
            @Nullable @Field("classDiv") String classDiv,
            @Nullable @Field("subjectDiv") String subjectDiv,
            @Nullable @Field("dept") String dept,
            @Nullable @Field("deptDiv") String deptDiv,
            @Nullable @Field("prof_nm") String prof_nm
    );

    @FormUrlEncoded
    @Headers({"Content-Type: charset=EUC-KR"})
    @POST("api.ApiApiMainBd.oapi")
    Task<List<UnivScheduleItem>> schedules(
            @NonNull @Field("apiKey") String apiKey
    );
}
