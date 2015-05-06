package com.uoscs09.theuos2.parse;


import android.support.annotation.NonNull;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

public abstract class OApiParser2<T> extends XmlParser<T> {

    protected abstract T initItem();

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

            T item = initItem();

            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }

                String name = parser.getName();
                if (name.equals(TAG_LIST)) {
                    readListParentTag(parser, item);

                } else {
                    skip(parser);

                }

            }

            if (item instanceof Parsable) {
                ((Parsable) item).afterParsing();
            }

            return item;
        }


        protected void readListParentTag(XmlPullParser parser, T item) throws IOException, XmlPullParserException {
            int count = 0;
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                switch (parser.getName()) {
                    case LIST:
                        try {
                            parser.require(XmlPullParser.START_TAG, null, LIST);
                            readList(parser, count, item);

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

        protected abstract void readList(XmlPullParser parser, int count, T item) throws IOException, XmlPullParserException;

    }


    public static abstract class ReflectionParser<T> extends SimpleListParser<ArrayList<T>> {

        @Override
        protected ArrayList<T> initItem() {
            return new ArrayList<>();
        }

        @NonNull
        protected abstract T newInstance();

        @Override
        protected void readList(XmlPullParser parser, int count, ArrayList<T> item) throws IOException, XmlPullParserException {
            item.add(readListAndFillDataUsingReflection(parser, newInstance()));
        }

    }

}
