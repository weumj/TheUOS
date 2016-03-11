package com.uoscs09.theuos2.oapi;


import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

class XmlConverterFactory extends Converter.Factory {
    public static XmlConverterFactory create() {
        return new XmlConverterFactory();
    }

    private XmlConverterFactory() {
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        // List<Type1>
        if (!(type instanceof ParameterizedType)) {
            return null;
        }

        Class<?> cls = (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0];
        return new XmlConverter<>(cls);
    }


}
