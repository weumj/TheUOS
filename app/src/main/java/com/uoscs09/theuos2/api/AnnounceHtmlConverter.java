package com.uoscs09.theuos2.api;

import com.uoscs09.theuos2.tab.announce.AnnounceItem;
import com.uoscs09.theuos2.tab.announce.AnnounceParser;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Converter;

class AnnounceHtmlConverter {
    private static AnnounceParser n, s;

    private static boolean annotationCheck(Annotation annotation, Class clazz) {
        return annotation.annotationType().getName().equals(clazz.getName());
    }

    static Converter<ResponseBody, List<AnnounceItem>> choose(Annotation[] annotations) {
        boolean mobile = false, scholarship = false;

        for (Annotation annotation : annotations) {
            if (annotationCheck(annotation, AnnounceApi.Mobile.class)) {
                mobile = true;
            }

            if (annotationCheck(annotation, AnnounceApi.Scholarship.class)) {
                scholarship = true;
            }
        }


        if (mobile) {
            return null;
        } else if (scholarship) {
            if (s == null) s = AnnounceParser.scholarship();
            return value -> {
                try {
                    return s.parse(value.string());
                } catch (IOException e) {
                    throw e;
                } catch (Throwable e) {
                    e.printStackTrace();
                    throw new IOException(e.getMessage());
                }
            };
        } else {
            if (n == null) n = AnnounceParser.normalWeb();
            return value -> {
                try {
                    return n.parse(value.string());
                } catch (IOException e) {
                    throw e;
                } catch (Throwable e) {
                    e.printStackTrace();
                    throw new IOException(e.getMessage());
                }
            };
        }

    }


}
