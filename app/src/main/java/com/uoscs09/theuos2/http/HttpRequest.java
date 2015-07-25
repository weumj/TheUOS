package com.uoscs09.theuos2.http;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;
import android.util.Log;

import com.uoscs09.theuos2.async.Processor;
import com.uoscs09.theuos2.async.Request;
import com.uoscs09.theuos2.util.StringUtil;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;

public abstract class HttpRequest<T> extends Request.Base<T> {
    //public static final int RETURN_TYPE_STRING = 0;
    // public static final int RETURN_TYPE_CONNECTION = 1;
    public static final int HTTP_METHOD_GET = 0;
    public static final int HTTP_METHOD_POST = 1;

    final String url;
    private final String encodedParams;
    final String resultEncoding;
    private final int method;

    HttpRequest(String url, String encodedParams, String resultEncoding, int method) {
        this.url = url;
        this.resultEncoding = resultEncoding;
        this.encodedParams = encodedParams;
        this.method = method;
    }

    public final HttpRequest<T> checkNetworkState(Context context) throws IOException {
        checkNetworkStateAndThrowException(context);
        return this;
    }

    HttpURLConnection getHttpResult() throws IOException {
        HttpURLConnection connection;

        if (method == HTTP_METHOD_GET && encodedParams != null)
            connection = getConnection(url + '?' + encodedParams);
        else
            connection = getConnection(url);

        if (method == HTTP_METHOD_POST)
            setUpPostSetting(connection, encodedParams);

        checkResponseAndThrowException(connection);

        return connection;

    }

    private static void setUpPostSetting(HttpURLConnection connection, @Nullable String encodedParams) throws IOException {
        connection.setDefaultUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("content-type", "application/x-www-form-urlencoded");

        if (encodedParams != null) {
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            out.write(encodedParams.getBytes());
            out.close();
        }

    }

    private static HttpURLConnection getConnection(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

        if (connection == null) {
            throw new IOException("HttpURLConnection returns null.");
        }
        connection.setConnectTimeout(5000);

        return connection;
    }

    private static class StringRequest extends HttpRequest<String> {

        private StringRequest(String url, String encodedParams, String resultEncoding, int method) {
            super(url, encodedParams, resultEncoding, method);
        }

        @Override
        public String getInner() throws IOException {

            HttpURLConnection connection = null;
            try {
                connection = getHttpResult();
                return readContentFromStream(connection.getInputStream(), resultEncoding);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

        }
    }

    private static class ConnectionRequest extends HttpRequest<HttpURLConnection> {

        public ConnectionRequest(String url, String encodedParams, String resultEncoding, int method) {
            super(url, encodedParams, resultEncoding, method);
        }

        @Override
        public HttpURLConnection getInner() throws IOException {
            return getHttpResult();
        }

    }

    public static abstract class Builder<T> implements com.uoscs09.theuos2.async.Request.Builder<T> {
        String url;
        String resultEncoding = StringUtil.ENCODE_UTF_8, paramsEncoding = StringUtil.ENCODE_UTF_8;
        Map<? extends CharSequence, ? extends CharSequence> params = null;
        int method = HTTP_METHOD_GET;

        public static Builder<String> newStringRequestBuilder(String url) {
            return new StringBuilder(url);
        }

        public static Builder<HttpURLConnection> newConnectionRequestBuilder(String url) {
            return new ConnectionBuilder(url);
        }

        Builder(String url) {
            this.url = url;
        }

        public Builder<T> setURL(String url) {
            this.url = url;
            return this;
        }

        public Builder<T> setResultEncoding(String encoding) {
            resultEncoding = encoding;
            return this;
        }

        public Builder<T> setParamsEncoding(String encoding) {
            paramsEncoding = encoding;
            return this;
        }

        public Builder<T> setParams(Map<? extends CharSequence, ? extends CharSequence> params) {
            this.params = params;
            return this;
        }

        public Builder<T> setHttpMethod(int method) {
            this.method = method;
            return this;
        }

        @Nullable
        String encodeParams() {
            if (params == null)
                return null;

            try {
                return encodeString(params, paramsEncoding);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return null;
            }

        }

        @Override
        public abstract HttpRequest<T> build();

        private static class StringBuilder extends Builder<String> {
            StringBuilder(String url) {
                super(url);
            }

            @Override
            public HttpRequest<String> build() {
                return new StringRequest(url, encodeParams(), resultEncoding, method);
            }
        }

        private static class ConnectionBuilder extends Builder<HttpURLConnection> {
            ConnectionBuilder(String url) {
                super(url);
            }

            @Override
            public HttpRequest<HttpURLConnection> build() {
                return new ConnectionRequest(url, encodeParams(), resultEncoding, method);
            }
        }

    }

    public static class FileDownProcessor implements Processor<HttpURLConnection, File> {
        private File downloadDir;

        public FileDownProcessor(File downloadDir) {
            this.downloadDir = downloadDir;
        }

        @Override
        public File process(HttpURLConnection connection) throws Exception {
            String responseHeaderFileName = connection.getHeaderField("content-disposition");

            String fileNameAndExtension = URLDecoder.decode(responseHeaderFileName.substring(responseHeaderFileName.indexOf("filename=") + 9), StringUtil.ENCODE_UTF_8).trim();
            int dotIndex = fileNameAndExtension.lastIndexOf('.');
            String fileName, extension;
            if (dotIndex != -1) {
                fileName = fileNameAndExtension.substring(0, dotIndex);
                extension = fileNameAndExtension.substring(dotIndex);
            } else {
                fileName = fileNameAndExtension;
                extension = "";
            }

            File downloadFile = new File(downloadDir, fileNameAndExtension);

            while (!downloadFile.createNewFile()) {
                fileName += "_1";
                fileNameAndExtension = fileName + extension;
                downloadFile = new File(downloadDir, fileNameAndExtension);
            }

            final FileOutputStream fileOutputStream = new FileOutputStream(downloadFile);
            final byte buffer[] = new byte[16 * 1024];

            final InputStream inputStream = connection.getInputStream();

            int len = 0;
            while ((len = inputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, len);
            }

            fileOutputStream.flush();
            fileOutputStream.close();

            return downloadFile;
        }
    }

    public static boolean checkNetworkUnable(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        return networkInfo == null || !networkInfo.isConnected();
    }

    static void checkNetworkStateAndThrowException(Context context) throws IOException {
        context = context.getApplicationContext();
        if (checkNetworkUnable(context))
            throw new IOException("Failed to access current network.");
    }

    @Nullable
    public static String encodeString(@Nullable Map<? extends CharSequence, ? extends CharSequence> table, @Nullable String encoding) throws UnsupportedEncodingException {
        if (table == null)
            return null;

        if (encoding == null)
            encoding = StringUtil.ENCODE_UTF_8;

        StringBuilder sb = new StringBuilder();
        final char eq = '=', amp = '&';
        for (Entry<? extends CharSequence, ? extends CharSequence> entry : table.entrySet()) {
            sb.append(URLEncoder.encode(entry.getKey().toString(), encoding)).append(eq)
                    .append(URLEncoder.encode(entry.getValue().toString(), encoding))
                    .append(amp);
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
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

            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }
            return builder.toString();
        } finally {
            reader.close();
        }
    }

}
