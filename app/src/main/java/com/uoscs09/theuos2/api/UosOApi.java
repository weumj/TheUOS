package com.uoscs09.theuos2.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.uoscs09.theuos2.tab.buildings.BuildingRoom;
import com.uoscs09.theuos2.tab.buildings.ClassRoomTimetable;
import com.uoscs09.theuos2.tab.emptyroom.EmptyRoomWrapper;
import com.uoscs09.theuos2.tab.schedule.UnivScheduleWrapper;
import com.uoscs09.theuos2.tab.subject.CoursePlanWrapper;
import com.uoscs09.theuos2.tab.subject.SubjectWrapper;
import com.uoscs09.theuos2.tab.subject.TimeTableSubjectInfo;

import mj.android.utils.task.Task;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface UosOApi {
    String URL = "http://wise.uos.ac.kr/uosdoc/";

    @FormUrlEncoded
    @Headers({"Content-Type: charset=EUC-KR"})
    @POST("api.ApiUcsFromToEmptyRoom.oapi")
    Task<EmptyRoomWrapper> emptyRooms(
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
    Task<CoursePlanWrapper> coursePlans(
            @NonNull @Field("apiKey") String apiKey,
            @NonNull @Field("term") String term,
            @NonNull @Field("subjectNo") String subjectNo,
            @NonNull @Field("classDiv") String classDiv,
            @NonNull @Field("year") String year
    );

    @Headers({"Content-Type: charset=EUC-KR"})
    @GET("api.ApiUcrCultTimeInq.oapi")
    Task<TimeTableSubjectInfo> timetableCulture(
            @NonNull @Query("apiKey") String apiKey,
            @NonNull @Query("year") String year,
            @NonNull @Query("term") String term,
            @NonNull @Query("subjectDiv") String subjectDiv,
            @Nullable @Query("subjectSubDiv") String subjectSubDiv,
            @Nullable @Query(value = "subjectNm", encoded = true) String subjectNm
    );

    @Headers({"Content-Type: charset=EUC-KR"})
    @GET("api.ApiUcrMjTimeInq.oapi")
    Task<TimeTableSubjectInfo> timetableMajor(
            @NonNull @Query("apiKey") String apiKey,
            @NonNull @Query("year") String year,
            @NonNull @Query("term") String term,
            @NonNull @Query("deptDiv") String deptDiv,
            @NonNull @Query("dept") String dept,
            @NonNull @Query("subDept") String subDept,
            @Nullable @Query("subjectDiv") String subjectDiv,
            @Nullable @Query("subjectNo") String subjectNo,
            @Nullable @Query("classDiv") String classDiv,
            @Nullable @Query(value = "subjectNm",encoded = true) String subjectNm,
            @Nullable @Query("etcExc") String bEtcExcludeYN
    );


    @Headers({"Content-Type: charset=EUC-KR"})
    @GET("api.ApiApiSubjectList.oapi")
    Task<SubjectWrapper> subjectInformation(
            @NonNull @Query("apiKey") String apiKey,
            @Query("year") int year,
            @NonNull @Query("term") String term,
            @NonNull @Query(value = "subjectNm", encoded = true) String subjectNm,
            @Nullable @Query("subjectNo") String subjectNo,
            @Nullable @Query("classDiv") String classDiv,
            @Nullable @Query("subjectDiv") String subjectDiv,
            @Nullable @Query("dept") String dept,
            @Nullable @Query("deptDiv") String deptDiv,
            @Nullable @Query(value = "prof_nm",encoded = true) String profName
    );

    @FormUrlEncoded
    @Headers({"Content-Type: charset=EUC-KR"})
    @POST("api.ApiApiMainBd.oapi")
    Task<UnivScheduleWrapper> schedules(
            @NonNull @Field("apiKey") String apiKey
    );

    @FormUrlEncoded
    @Headers({"Content-Type: charset=EUC-KR"})
    @POST("api.ApiApiBuildingRoomList.oapi")
    Task<BuildingRoom> buildings(
            @NonNull @Field("apiKey") String apiKey
    );

    @FormUrlEncoded
    @Headers({"Content-Type: charset=EUC-KR"})
    @POST("api.ApiUcsCourseTimeTableRoomList.oapi")
    Task<ClassRoomTimetable> classRoomTimeTables(
            @Field("apiKey") String apiKey,
            @Field("year") String year,
            @Field("term") String term,
            @Field("building") String building,
            @Field("classRoom") String classRoom
    );
}
