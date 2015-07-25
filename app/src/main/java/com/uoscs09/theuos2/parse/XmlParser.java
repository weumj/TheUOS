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
import java.util.ArrayList;

public abstract class XmlParser<T> extends IParser.Base<InputStream, T> {

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
                default:
                    break;
            }
        }

    }

    protected static <Data> void readListAndFillObject(XmlPullParser parser, @NonNull Data newInstance) throws IOException, XmlPullParserException {
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
                    f.setInt(object, text == null ? 0 : Integer.parseInt(text));
                    return true;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;

            default:
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

        if (newInstance instanceof AfterParsable) {
            ((AfterParsable) newInstance).afterParsing();
        }

        return newInstance;
    }

    public static <T> XmlParser<ArrayList<T>> newReflectionParser(Class<? extends T> clazz, String docRootTag, String listParentTag, String listItemTag) {
        return new SimpleReflectionParser<>(clazz, docRootTag, listParentTag, listItemTag);
    }

    abstract static class SimpleListParser<T> extends XmlParser<T> {
        private final String TAG_LIST_PARENT, TAG_LIST_ITEM, TAG_DOC_ROOT;

        public SimpleListParser(String docRootTag, String listParentTag, String listItemTag) {
            TAG_DOC_ROOT = docRootTag;
            TAG_LIST_PARENT = listParentTag;
            TAG_LIST_ITEM = listItemTag;
        }

        protected abstract T initItem();

        @Override
        protected T parseContent(XmlPullParser parser) throws IOException, XmlPullParserException {

            parser.require(XmlPullParser.START_TAG, null, TAG_DOC_ROOT);

            T item = initItem();

            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }

                String name = parser.getName();
                if (name.equals(TAG_LIST_PARENT)) {
                    readListContentTag(parser, item);

                } else {
                    skip(parser);

                }

            }

            if (item instanceof AfterParsable) {
                ((AfterParsable) item).afterParsing();
            }

            return item;
        }


        protected void readListContentTag(XmlPullParser parser, T item) throws IOException, XmlPullParserException {
            int count = 0;
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }

                String name = parser.getName();
                if (name.equals(TAG_LIST_ITEM)) {
                    parser.require(XmlPullParser.START_TAG, null, TAG_LIST_ITEM);
                    readList(parser, count, item);
                    count++;

                } else {
                    skip(parser);

                }

            }

        }

        protected abstract void readList(XmlPullParser parser, int count, T item) throws IOException, XmlPullParserException;

    }


    static class SimpleReflectionParser<T> extends SimpleListParser<ArrayList<T>> {

        private final Class<? extends T> clazz;

        public SimpleReflectionParser(Class<? extends T> clazz, String docRootTag, String listParentTag, String listItemTag) {
            super(docRootTag, listParentTag, listItemTag);
            this.clazz = clazz;
        }

        @Override
        protected ArrayList<T> initItem() {
            return new ArrayList<>();
        }

        @NonNull
        protected T newInstance() {
            try {
                return clazz.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected void readList(XmlPullParser parser, int count, ArrayList<T> item) throws IOException, XmlPullParserException {
            item.add(readListAndFillDataUsingReflection(parser, newInstance()));
        }

    }

}
