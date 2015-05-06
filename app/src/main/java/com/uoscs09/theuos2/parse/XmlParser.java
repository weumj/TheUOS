package com.uoscs09.theuos2.parse;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.uoscs09.theuos2.util.StringUtil;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

public abstract class XmlParser<T> implements IParser<InputStream, T> {

    public static interface Parsable {
        public void afterParsing();
    }

    @Override
    public T parse(InputStream is) throws Exception {
        try {
            XmlPullParser pullParser = XmlPullParserFactory.newInstance().newPullParser();

            pullParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            pullParser.setInput(is, null);
            pullParser.nextTag();

            return parseContent(pullParser);

        } finally {
            is.close();
        }

    }

    protected abstract T parseContent(XmlPullParser parser) throws IOException, XmlPullParserException;

    @Nullable
    protected static String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = null;
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    public static void skip(XmlPullParser parser) throws IOException, XmlPullParserException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }

    }

    protected static <Data> void readListAndFillObject(XmlPullParser parser, Data newInstance) throws IOException, XmlPullParserException {
        Class<?> clazz = newInstance.getClass();

        Field[] fields = clazz.getDeclaredFields();
        for (Field f : fields) {
            f.setAccessible(true);
        }

        SetObject:
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String tagName = parser.getName();

            for (Field f : fields) {
                if (f.getName().equals(tagName)) {
                    if (setObjectData(f, newInstance, parser))
                        continue SetObject;
                }
            }

            skip(parser);

        }

    }

    private static boolean setObjectData(Field f, Object object, XmlPullParser parser) {
        Class<?> type = f.getType();

        String text;

        try {
            text = readText(parser);
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
            return false;
        }

        switch (type.getSimpleName()) {
            case "String":
                try {
                    f.set(object, text == null ? StringUtil.NULL : removeCDATA(text));
                    return true;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;

            case "int":
                try {
                    f.setInt(object, text == null ? 0 : Integer.valueOf(text));
                    return true;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
        }

        /*
        if (type.equals(String.class)) {

            try {
                f.set(object, text == null ? StringUtil.NULL : removeCDATA(text));
                return true;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        } else if (type.equals(Integer.TYPE)) {
            try {
                f.setInt(object, text == null ? 0 : Integer.valueOf(text));
                return true;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        */

        return false;

    }

    private static final String CDATA_HEAD = "<![CDATA[";
    private static final String CDATA_TAIL = "]]>";

    protected static String removeCDATA(@NonNull String in) {
        return in.replace(CDATA_HEAD, StringUtil.NULL).replace(CDATA_TAIL, StringUtil.NULL).trim();
    }

    protected static <Data> Data readListAndFillDataUsingReflection(XmlPullParser parser, @NonNull Data newInstance) throws IOException, XmlPullParserException {
        readListAndFillObject(parser, newInstance);

        if (newInstance instanceof Parsable) {
            ((Parsable) newInstance).afterParsing();
        }

        return newInstance;
    }

}
