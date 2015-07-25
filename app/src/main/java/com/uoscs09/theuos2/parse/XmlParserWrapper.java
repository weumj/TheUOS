package com.uoscs09.theuos2.parse;


import java.net.HttpURLConnection;

public class XmlParserWrapper<T> extends IParser.Base<HttpURLConnection, T> {
    private final XmlParser<T> parser;

    public XmlParserWrapper(XmlParser<T> parser) {
        this.parser = parser;
    }


    @Override
    public T parse(HttpURLConnection connection) throws Exception {

        try {
            return parser.parse(connection.getInputStream());
        } finally {
            connection.disconnect();
        }

    }
}
