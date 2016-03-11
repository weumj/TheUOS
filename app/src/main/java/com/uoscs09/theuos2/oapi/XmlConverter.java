package com.uoscs09.theuos2.oapi;

import com.uoscs09.theuos2.parse.XmlParser;
import com.uoscs09.theuos2.tab.schedule.UnivScheduleItem;

import java.io.IOException;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Converter;


class XmlConverter<T> implements Converter<ResponseBody, List<T>> {

    private Class<T> clazz;

    XmlConverter(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public List<T> convert(ResponseBody value) throws IOException {
        try {
            XmlParser<List<T>> parser;
            if (clazz.equals(UnivScheduleItem.class)) {
                parser = XmlParser.newReflectionParser(clazz, "euc-kr", "root", "schList", "list");
            } else
                parser = XmlParser.newReflectionParser(clazz, "euc-kr", "root", "mainlist", "list");

            return parser.parse(value.byteStream());
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }
    }


}
