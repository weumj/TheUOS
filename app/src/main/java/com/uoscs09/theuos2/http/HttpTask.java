package com.uoscs09.theuos2.http;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.uoscs09.theuos2.util.IOUtil;
import com.uoscs09.theuos2.util.NetworkUtil;
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

import rx.Observable;

/**
 * Http 요청을 처리하는 Request 클래스
 */
public final class HttpTask {
    //public static final int RETURN_TYPE_STRING = 0;
    // public static final int RETURN_TYPE_CONNECTION = 1;
    public static final int HTTP_METHOD_GET = 0;
    public static final int HTTP_METHOD_POST = 1;

    public static void checkNetworkStateAndThrowException() throws IOException {
        if (!NetworkUtil.isConnectivityEnable())
            throw new IOException("Failed to access current network.");
    }

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
            Log.e("HttpTask", connection.getURL().toExternalForm() + " / response : " + response);
            throw new IOException("HttpURLConnection responses bad result.");
        }
    }

    public static String readContentFromStream(InputStream in, String encoding) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, encoding));
        try {
            StringBuilder builder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }
            return builder.toString();
        } finally {
            IOUtil.closeStream(reader);
        }
    }


    private static class HttpRequest {
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

        /**
         * Http 요청을 전송한다.
         */
        HttpURLConnection getHttpResult() throws IOException {
            checkNetworkStateAndThrowException();

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

        /**
         * Post 연결 설정을 한다.
         */
        void setUpPostSetting(HttpURLConnection connection, @Nullable String encodedParams) throws IOException {
            connection.setDefaultUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("content-type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("accept-encoding", "gzip, deflate");

            if (encodedParams != null) {
                DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                out.write(encodedParams.getBytes());
                out.close();
            }

        }

        //**** util method

        private static HttpURLConnection getConnection(String url) throws IOException {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

            if (connection == null) {
                throw new IOException("HttpURLConnection returns null.");
            }
            connection.setConnectTimeout(5000);

            return connection;
        }
    }


    //** Request Impl
    private static class StringRequest extends HttpRequest {

        StringRequest(String url, String encodedParams, String resultEncoding, int method) {
            super(url, encodedParams, resultEncoding, method);
        }

        public String get() throws IOException {

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

    private static class ConnectionRequest extends HttpRequest {

        ConnectionRequest(String url, String encodedParams, String resultEncoding, int method) {
            super(url, encodedParams, resultEncoding, method);
        }

        public HttpURLConnection get() throws IOException {
            return getHttpResult();
        }

    }

    //** Request Builder

    public static class Builder {
        String url;
        String resultEncoding = StringUtil.ENCODE_UTF_8, paramsEncoding = StringUtil.ENCODE_UTF_8;
        Map<? extends CharSequence, ? extends CharSequence> params = null;
        int method = HTTP_METHOD_GET;

        public Observable<String> buildAsString() {
            return Observable.fromCallable(new StringRequest(url, encodeParams(), resultEncoding, method)::get);
        }

        public Observable<HttpURLConnection> buildAsHttpURLConnection() {
            return Observable.fromCallable(new ConnectionRequest(url, encodeParams(), resultEncoding, method)::get);
        }

        public Builder(String url) {
            this.url = url;
        }

        /*
        public Builder<T> setURL(String url) {
            this.url = url;
            return this;
        }
        */

        public Builder setResultEncoding(String encoding) {
            resultEncoding = encoding;
            return this;
        }

        public Builder setParamsEncoding(String encoding) {
            paramsEncoding = encoding;
            return this;
        }

        public Builder setParams(Map<? extends CharSequence, ? extends CharSequence> params) {
            this.params = params;
            return this;
        }

        public Builder setHttpMethod(int method) {
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

    }


    //** FileDownloadProcessor

    public static class FileDownloadProcessor {
        private final File downloadDir;
        private final String suggestFileName;

        public FileDownloadProcessor(File downloadDir, @Nullable String suggestFileName) {
            this.downloadDir = downloadDir;
            this.suggestFileName = suggestFileName;
        }

        public File func(HttpURLConnection connection) throws IOException {
            String fileNameAndExtension = TextUtils.isEmpty(suggestFileName) ? getFileName(connection) : suggestFileName;
            File downloadFile = makeFile(fileNameAndExtension);

            writeContentsToFile(downloadFile, connection.getInputStream());
            connection.disconnect();

            return downloadFile;
        }

        /**
         * Http Response Header 에서 파일 이름을 가져온다.
         *
         * @param connection Http 연결
         * @return 파일 이름과 확장자로 이루어진 문자열
         */
        private String getFileName(HttpURLConnection connection) throws UnsupportedEncodingException {
            String responseHeaderFileName = connection.getHeaderField("content-disposition").replace("\"", "");

            return URLDecoder.decode(
                    responseHeaderFileName.substring(responseHeaderFileName.indexOf("filename=") + 9), StringUtil.ENCODE_UTF_8)
                    .trim();
        }

        /**
         * 주어진 파일 이름과 확장자로 이루어진 문자열로 부터 파일을 생성한다.
         */
        private File makeFile(String fileNameAndExtension) {
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

            while (true) {
                try {
                    while (!downloadFile.createNewFile()) {
                        // 파일이 이미 존재하면 이름에 '_1' 을 덧붙여 파일을 생성한다.
                        fileName += "_1";
                        fileNameAndExtension = fileName + extension;
                        downloadFile = new File(downloadDir, fileNameAndExtension);
                    }
                    break;

                } catch (IOException e) {
                    // 파일 이름이 이상해서 파일 생성에 실패한 경우
                    // 주어진 이름으로 파일을 생성한다.
                    // e.printStackTrace();
                    fileName = "the_uos_download_file";
                    fileNameAndExtension = fileName + extension;
                    downloadFile = new File(downloadDir, fileNameAndExtension);
                }

            }

            return downloadFile;

        }

        /**
         * 파일에 내용을 기록한다.
         *
         * @param downloadFile 다운로드하는 내용이 기록될 파일
         * @param inputStream  Http 연결의 내용이 있는 스트림
         */
        private void writeContentsToFile(File downloadFile, InputStream inputStream) throws IOException {
            final FileOutputStream fileOutputStream = new FileOutputStream(downloadFile);
            final byte buffer[] = new byte[16 * 1024];

            try {
                int len;
                while ((len = inputStream.read(buffer)) > 0) {
                    fileOutputStream.write(buffer, 0, len);
                }

                fileOutputStream.flush();
            } finally {
                IOUtil.closeStream(fileOutputStream);
                IOUtil.closeStream(inputStream);
            }

        }

    }

}
