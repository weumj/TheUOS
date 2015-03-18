package com.uoscs09.theuos.http;

import android.util.Log;

import com.uoscs09.theuos.common.util.StringUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;

public class HttpRequest {
    public static String getBody(String url, String encoding, Map<? extends CharSequence, ? extends CharSequence> params) throws IOException {
        return getBody(url, encoding, params, encoding);
    }

    public static String getBody(String url, String encoding, Map<? extends CharSequence, ? extends CharSequence> params, String paramsEncoding) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(url).append('?');
        encodeString(sb, params, paramsEncoding);
        return getBody(sb.toString(), encoding);
    }

    public static String getBodyByPost(String url, String encoding, Map<? extends CharSequence, ? extends CharSequence> params, String paramsEncoding) throws IOException {
        StringBuilder sb = new StringBuilder();
        encodeString(sb, params, paramsEncoding);
        return getBodyByPost(url, sb.toString(), encoding);
    }

    public static String getBody(String url, Map<? extends CharSequence, ? extends CharSequence> params) throws IOException {
        return getBody(url, StringUtil.ENCODE_UTF_8, params);
    }

    public static StringBuilder encodeString(StringBuilder b, Map<? extends CharSequence, ? extends CharSequence> table, String encoding) throws UnsupportedEncodingException {
        final char eq = '=', amp = '&';
        for (Entry<? extends CharSequence, ? extends CharSequence> entry : table.entrySet()) {
            b.append(URLEncoder.encode(entry.getKey().toString(), encoding)).append(eq)
                    .append(URLEncoder.encode(entry.getValue().toString(), encoding))
                    .append(amp);
        }
        b.deleteCharAt(b.length() - 1);
        return b;
    }

    public static StringBuilder encodeString(StringBuilder b, Map<? extends CharSequence, ? extends CharSequence> params) throws UnsupportedEncodingException {
        return encodeString(b, params, StringUtil.ENCODE_UTF_8);
    }

    public static String getBody(String url) throws IOException {
        return getBody(url, StringUtil.ENCODE_UTF_8);
    }

    public static String getBody(String url, String encoding) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url)
                .openConnection();
        try {
            if (connection == null) {
                throw new IOException("HttpURLConnection returns null.");
            }

            connection.setConnectTimeout(5000);

            checkResponseAndThrowException(connection);

            return readContentFromStream(connection.getInputStream(), encoding);
        } finally {
            connection.disconnect();
        }
    }

    public static String getBodyByPost(String url, String params, String encoding) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url)
                .openConnection();
        try {
            if (connection == null) {
                throw new IOException("HttpURLConnection returns null.");
            }

            connection.setConnectTimeout(5000);
            connection.setDefaultUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("content-type", "application/x-www-form-urlencoded");

            PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                    connection.getOutputStream()));
            pw.write(params);
            pw.flush();

            checkResponseAndThrowException(connection);

            return readContentFromStream(connection.getInputStream(), encoding);
        } finally {
            connection.disconnect();
        }
    }

    static void checkResponseAndThrowException(HttpURLConnection connection) throws IOException {
        int response = connection.getResponseCode();
        if (response != HttpURLConnection.HTTP_OK) {
            Log.e("HttpRequest", connection.getURL().toExternalForm() + " / response : " + response);
            throw new IOException("HttpURLConnection responses bad result.");
        }
    }

    static String readContentFromStream(InputStream in, String encoding) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, encoding));
        try {
            StringBuilder builder = new StringBuilder();
            String line;
            final char c = '\n';
            while ((line = reader.readLine()) != null) {
                builder.append(line).append(c);
            }
            reader.close();
            return builder.toString();
        } finally {
            reader.close();
        }
    }

}
