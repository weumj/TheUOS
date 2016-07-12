package com.uoscs09.theuos2.api;

import android.support.v4.util.ArrayMap;

import com.uoscs09.theuos2.parse.JerichoParser;
import com.uoscs09.theuos2.tab.announce.AnnounceItem;
import com.uoscs09.theuos2.tab.announce.ParseAnnounce;
import com.uoscs09.theuos2.tab.booksearch.BookItem;
import com.uoscs09.theuos2.tab.booksearch.ParseBook;
import com.uoscs09.theuos2.tab.libraryseat.ParseSeat;
import com.uoscs09.theuos2.tab.libraryseat.SeatInfo;
import com.uoscs09.theuos2.tab.restaurant.ParseRest;
import com.uoscs09.theuos2.tab.restaurant.ParseRestaurantWeek;
import com.uoscs09.theuos2.tab.restaurant.RestItem;
import com.uoscs09.theuos2.tab.restaurant.WeekRestItem;

import java.io.IOException;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Converter;

class HtmlConverter implements Converter<ResponseBody, Object> {
    private static final Map<String, JerichoParser<?>> PARSER_MAP = new ArrayMap<>();
    private static final Map<String, String> CHARSET_MAP = new ArrayMap<>();

    private Class<?> clazz;

    HtmlConverter(Class<?> clazz) {
        this.clazz = clazz;
    }


    @Override
    public Object convert(ResponseBody value) throws IOException {
        JerichoParser<?> parser;

        String className = clazz.getName();

        if (PARSER_MAP.containsKey(className)) {
            parser = PARSER_MAP.get(className);
        } else {
            if (clazz.equals(BookItem.class))
                parser = new ParseBook();
            else if (clazz.equals(SeatInfo.class)) {
                parser = new ParseSeat();
                CHARSET_MAP.put(className, "euc-kr");
            } else if (clazz.equals(RestItem.class)) {
                parser = new ParseRest();
            } else if (clazz.equals(WeekRestItem.class))
                parser = new ParseRestaurantWeek();
            else if (clazz.equals(AnnounceItem.class)) {
                parser = ParseAnnounce.mobileWeb();
            } else
                throw new IOException("incompatible class input : " + clazz.getName());

            PARSER_MAP.put(className, parser);
        }

        try {
            if (CHARSET_MAP.containsKey(className)) {
                return parser.parse(new String(value.bytes(), CHARSET_MAP.get(className)));
            } else {
                return parser.parse(value.string());
            }
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }
    }
}
