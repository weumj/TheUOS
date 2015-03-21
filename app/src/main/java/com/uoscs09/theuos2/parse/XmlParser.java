package com.uoscs09.theuos2.parse;


import android.support.annotation.Nullable;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;

public abstract class XmlParser<T> implements IParser<InputStream, T> {

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

    protected static void skip(XmlPullParser parser) throws IOException, XmlPullParserException {
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
}
