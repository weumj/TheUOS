package com.uoscs09.theuos2.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.uoscs09.theuos2.tab.buildings.BuildingRoom;
import com.uoscs09.theuos2.tab.buildings.ClassRoomTimeTable;
import com.uoscs09.theuos2.tab.emptyroom.EmptyRoomInfo;
import com.uoscs09.theuos2.tab.schedule.UnivScheduleInfo;
import com.uoscs09.theuos2.tab.subject.CoursePlanInfo;
import com.uoscs09.theuos2.tab.subject.SubjectInformation;
import com.uoscs09.theuos2.tab.subject.TimeTableSubjectInfo;

import mj.android.utils.task.Task;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface UosOApi {
    String URL = "http://wise.uos.ac.kr/uosdoc/";

    @FormUrlEncoded
    @Headers({"Content-Type: charset=EUC-KR"})
    @POST("api.ApiUcsFromToEmptyRoom.oapi")
    Task<EmptyRoomInfo> emptyRooms(
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
    Task<CoursePlanInfo> coursePlans(
            @NonNull @Field("apiKey") String apiKey,
            @NonNull @Field("term") String term,
            @NonNull @Field("subjectNo") String subjectNo,
            @NonNull @Field("classDiv") String classDiv,
            @NonNull @Field("year") String year
    );

    @FormUrlEncoded
    @Headers({"Content-Type: charset=EUC-KR"})
    @POST("api.ApiUcrCultTimeInq.oapi")
    Task<TimeTableSubjectInfo> timetableCulture(
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
    Task<TimeTableSubjectInfo> timetableMajor(
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
    Task<SubjectInformation> subjectInformation(
            @NonNull @Field("apiKey") String apiKey,
            @Field("year") int year,
            @NonNull @Field("term") String term,
            @NonNull @Field("subjectNm") String subjectNm,
            @Nullable @Field("subjectNo") String subjectNo,
            @Nullable @Field("classDiv") String classDiv,
            @Nullable @Field("subjectDiv") String subjectDiv,
            @Nullable @Field("dept") String dept,
            @Nullable @Field("deptDiv") String deptDiv,
            @Nullable @Field("prof_nm") String profName
    );

    @FormUrlEncoded
    @Headers({"Content-Type: charset=EUC-KR"})
    @POST("api.ApiApiMainBd.oapi")
    Task<UnivScheduleInfo> schedules(
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
    Task<ClassRoomTimeTable> classRoomTimeTables(
            @Field("apiKey") String apiKey,
            @Field("year") String year,
            @Field("term") String term,
            @Field("building") String building,
            @Field("classRoom") String classRoom
    );
}
