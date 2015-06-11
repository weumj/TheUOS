package com.uoscs09.theuos2.parse;


import android.os.Parcel;
import android.os.Parcelable;
import android.text.Html;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

public abstract class ParseUosRSS<T> extends XmlParser<T> {

    private static final ParseUosRSS<ArrayList<Item>> parser = new ParseUOSRSSImpl();

    public static ParseUosRSS<ArrayList<Item>> getParser() {
        return parser;
    }

    protected abstract T initItem();

    @Override
    protected T parseContent(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "rss");

        T item = initItem();

        if (!visitTagAndGoChildTag(parser, "channel"))
            return item;

        int count = 0;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            switch (parser.getName()) {
                case "item":
                    try {
                        parser.require(XmlPullParser.START_TAG, null, "item");
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


        if (item instanceof AfterParsable) {
            ((AfterParsable) item).afterParsing();
        }

        return item;
    }

    protected boolean visitTagAndGoChildTag(XmlPullParser parser, String tag) throws IOException, XmlPullParserException {
        while (parser.next() != XmlPullParser.END_TAG) {
            switch (parser.getEventType()) {

                case XmlPullParser.END_DOCUMENT:
                    return false;

                case XmlPullParser.START_TAG:
                    break;

                default:
                    continue;
            }

            if (parser.getName().equals(tag)) {
                return true;

            } else {
                skip(parser);

            }
        }

        return false;
    }

    protected abstract void readList(XmlPullParser parser, int count, T item) throws IOException, XmlPullParserException;

    public static class Item implements AfterParsable, Parcelable {
        public String title, link, description, author, pubDate;

        public Item() {
        }

        public Item(Parcel in) {
            title = in.readString();
            link = in.readString();
            description = in.readString();
            author = in.readString();
            pubDate = in.readString();
        }

        @Override
        public void afterParsing() {
            description = Html.fromHtml(description).toString();
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(title);
            dest.writeString(link);
            dest.writeString(description);
            dest.writeString(author);
            dest.writeString(pubDate);
        }

        public static final Creator<Item> CREATOR = new Creator<Item>() {

            @Override
            public Item createFromParcel(Parcel source) {
                return new Item(source);
            }

            @Override
            public Item[] newArray(int size) {
                return new Item[size];
            }

        };
    }

    private static class ParseUOSRSSImpl extends ParseUosRSS<ArrayList<Item>> {

        @Override
        protected ArrayList<Item> initItem() {
            return new ArrayList<>();
        }

        @Override
        protected void readList(XmlPullParser parser, int count, ArrayList<Item> item) throws IOException, XmlPullParserException {
            item.add(readListAndFillDataUsingReflection(parser, new Item()));
        }
    }
}
