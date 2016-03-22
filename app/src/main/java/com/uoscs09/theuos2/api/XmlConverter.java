package com.uoscs09.theuos2.api;

import android.support.v4.util.ArrayMap;

import com.uoscs09.theuos2.parse.XmlParser2;

import java.io.IOException;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Converter;


class XmlConverter implements Converter<ResponseBody, Object> {

    private Class<?> clazz;
    private static final Map<Class, XmlParser2> parserMap = new ArrayMap<>();

    XmlConverter(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Object convert(ResponseBody value) throws IOException {
        try {
            XmlParser2<?> parser;
            if (parserMap.containsKey(clazz)) {
                parser = (XmlParser2<?>) parserMap.get(clazz);
            } else {
                parser = new XmlParser2<>(clazz);
                parserMap.put(clazz, parser);
            }

            return parser.parse(value.byteStream());
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }
    }


}
