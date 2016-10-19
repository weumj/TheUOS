package com.uoscs09.theuos2.api;

import com.uoscs09.theuos2.tab.announce.AnnounceItem;
import com.uoscs09.theuos2.tab.booksearch.BookDetailItem;
import com.uoscs09.theuos2.tab.booksearch.BookItem;
import com.uoscs09.theuos2.tab.booksearch.BookStates;
import com.uoscs09.theuos2.tab.buildings.BuildingRoom;
import com.uoscs09.theuos2.tab.buildings.ClassroomTimeTable;
import com.uoscs09.theuos2.tab.emptyroom.EmptyRoomInfo;
import com.uoscs09.theuos2.tab.libraryseat.SeatInfo;
import com.uoscs09.theuos2.tab.restaurant.RestItem;
import com.uoscs09.theuos2.tab.restaurant.WeekRestItem;
import com.uoscs09.theuos2.tab.schedule.UnivScheduleInfo;
import com.uoscs09.theuos2.tab.subject.CoursePlanInfo;
import com.uoscs09.theuos2.tab.subject.SubjectInformation;
import com.uoscs09.theuos2.tab.subject.TimeTableSubjectInfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

class ConverterFactory extends Converter.Factory {
    private static ConverterFactory instance;

    public static ConverterFactory getInstance() {
        if (instance == null)
            instance = new ConverterFactory();

        return instance;
    }

    private ConverterFactory() {
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        Class<?> cls;
        // List<Type1>
        if (type instanceof ParameterizedType) {
            cls = (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0];
        } else if (type instanceof Class) {
            cls = (Class<?>) type;
        } else {
            return null;
        }

        try {
            if (cls.equals(AnnounceItem.class)) {
                Converter<ResponseBody, ?> converter = AnnounceHtmlConverter.choose(annotations);
                return converter == null ? new HtmlConverter(cls) : converter;
            } else if (cls.equals(BookItem.class)
                    || cls.equals(SeatInfo.class)
                    || cls.equals(RestItem.class)
                    || cls.equals(WeekRestItem.class)
                    || cls.equals(BookDetailItem.class)) {
                return new HtmlConverter(cls);
            } else if (cls.equals(UnivScheduleInfo.class)
                    || cls.equals(CoursePlanInfo.class)
                    || cls.equals(EmptyRoomInfo.class)
                    || cls.equals(TimeTableSubjectInfo.class)
                    || cls.equals(SubjectInformation.class)
                    || cls.equals(BookStates.class)
                    || cls.equals(BuildingRoom.class)
                    || cls.equals(ClassroomTimeTable.class)) {
                return new XmlConverter(cls);
            }
            return null;
        } catch (IllegalStateException e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
