package com.uoscs09.theuos.http.parse;


import android.support.annotation.NonNull;

import com.uoscs09.theuos.util.StringUtil;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;

public abstract class OApiParser2<T> extends XmlParser<T> {

    private static final String CDATA_HEAD = "<![CDATA[";
    private static final String CDATA_TAIL = "]]>";


    protected static String removeCDATA(String in) {
        return in.replace(CDATA_HEAD, StringUtil.NULL).replace(CDATA_TAIL, StringUtil.NULL).trim();
    }

    public abstract static class SimpleListParser<T> extends OApiParser2<T> {
        private final String TAG_LIST;
        private static final String LIST = "list";

        public SimpleListParser() {
            TAG_LIST = getListTag();
        }

        @NonNull
        public abstract String getListTag();

        @Override
        protected T parseContent(XmlPullParser parser) throws IOException, XmlPullParserException {
            parser.require(XmlPullParser.START_TAG, null, "root");

            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }

                String name = parser.getName();
                if (name.equals(TAG_LIST)) {
                    readListParentTag(parser);

                } else {
                    skip(parser);

                }

            }

            return makeItem();
        }

        protected void readListParentTag(XmlPullParser parser) throws IOException, XmlPullParserException {
            int count = 0;
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                switch (parser.getName()) {
                    case LIST:
                        try {
                            parser.require(XmlPullParser.START_TAG, null, LIST);
                            readList(parser, count);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        count++;

                        break;

                    default:
                        skip(parser);
                        break;
                }

            }
        }

        protected abstract void readList(XmlPullParser parser, int count) throws IOException, XmlPullParserException;

        protected abstract T makeItem();
    }


    public static abstract class ReflectionParser<T> extends SimpleListParser<ArrayList<T>> {
        protected ArrayList<T> list = new ArrayList<>();

        @Override
        protected ArrayList<T> parseContent(XmlPullParser parser) throws IOException, XmlPullParserException {
            list.clear();
            return super.parseContent(parser);
        }

        @NonNull
        protected abstract T newInstance();

        @NonNull
        protected abstract Class<? extends T> getReflectionClass(T instance);

        @Override
        protected void readList(XmlPullParser parser, int count) throws IOException, XmlPullParserException {

            T object = newInstance();
            Class<? extends T> clazz = getReflectionClass(object);

            Field[] fields = clazz.getDeclaredFields();
            for (Field f : fields) {
                f.setAccessible(true);
            }

            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }

                String tagName = parser.getName();

                boolean set = false;
                for (Field f : fields) {
                    if (f.getName().equals(tagName)) {
                        set = setObjectData(f, object, parser);

                        if (set)
                            break;
                    }
                }

                if (set)
                    continue;
                skip(parser);

            }

            list.add(object);
        }

        private boolean setObjectData(Field f, Object object, XmlPullParser parser) {
            Class<?> type = f.getType();

            if (type.equals(String.class)) {

                try {
                    f.set(object, removeCDATA(readText(parser)));
                    return true;
                } catch (IllegalAccessException | XmlPullParserException | IOException e) {
                    e.printStackTrace();
                }

            } else if (type.equals(Integer.class)) {
                try {
                    f.setInt(object, Integer.valueOf(readText(parser)));
                    return true;
                } catch (IllegalAccessException | XmlPullParserException | IOException e) {
                    e.printStackTrace();
                }
            }
            return false;

        }

        @Override
        protected ArrayList<T> makeItem() {
            return list;
        }
    }
}
