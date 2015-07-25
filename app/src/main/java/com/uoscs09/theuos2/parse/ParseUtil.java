package com.uoscs09.theuos2.parse;

public class ParseUtil {
    /*
    @Deprecated
    public static <T> T parseXml(Context context, XmlParser<T> parser, String url, String paramsEncoding, Map<? extends CharSequence, ? extends CharSequence> params) throws Exception {
        HttpURLConnection connection = HttpRequest.getConnection(context, url, paramsEncoding, params);

        try {
            return parser.parse(connection.getInputStream());
        } finally {
            connection.disconnect();
        }

    }

    @Deprecated
    public static <T> T parseXml(Context context, XmlParser<T> parser, String url, Map<? extends CharSequence, ? extends CharSequence> params) throws Exception {
        return parseXml(context, parser, url, StringUtil.ENCODE_EUC_KR, params);

    }

    @Deprecated
    public static <T> T parseXml(Context context, XmlParser<T> parser, String url) throws Exception {
        return parseXml(context, parser, url, StringUtil.ENCODE_EUC_KR, null);

    }



    public static <T> AsyncJob<T> makeParseJob(Context context, XmlParser<T> parser, String url, @Nullable String paramsEncoding, @Nullable Map<? extends CharSequence, ? extends CharSequence> params){

    }

    abstract class a<T> extends AsyncJob.Base<T>{
        XmlParser<T> parser;


        @Override
        public T call() throws Exception {
            HttpURLConnection connection = HttpRequest.getConnection(context, url, paramsEncoding, params);

            try {
                return parser.parse(connection.getInputStream());
            } finally {
                connection.disconnect();
            }
        }
    }

*/
}
