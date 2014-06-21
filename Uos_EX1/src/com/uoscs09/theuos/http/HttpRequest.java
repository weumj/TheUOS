package com.uoscs09.theuos.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Hashtable;

import com.uoscs09.theuos.common.util.StringUtil;

public class HttpRequest {
	public static String getBody(String url, String encoding,
			Hashtable<String, String> params) throws SocketTimeoutException,
			UnsupportedEncodingException, IOException {
		return getBody(url, encoding, params, encoding);
	}

	public static String getBody(String url, String encoding,
			Hashtable<String, String> params, String paramsEncoding)
			throws SocketTimeoutException, UnsupportedEncodingException,
			IOException {
		StringBuilder sb = new StringBuilder();
		sb.append(url).append('?');
		encodeString(sb, params, paramsEncoding);
		return getBody(sb.toString(), encoding);
	}
	public static String getBodyByPost(String url, String encoding,
			Hashtable<String, String> params, String paramsEncoding)
			throws SocketTimeoutException, UnsupportedEncodingException,
			IOException {
		StringBuilder sb = new StringBuilder();
		encodeString(sb, params, paramsEncoding);
		return getBodyByPost(url,sb.toString(), encoding);
	}

	public static String getBody(String url,
			Hashtable<String, String> params) throws SocketTimeoutException,
			UnsupportedEncodingException, IOException {
		return getBody(url, StringUtil.ENCODE_UTF_8, params);
	}

	public static StringBuilder encodeString(StringBuilder b,
			Hashtable<String, String> table, String encoding)
			throws UnsupportedEncodingException {
		Enumeration<String> keys = table.keys();

		String name, value;
		final char eq = '=', amp = '&'; 
		while (keys.hasMoreElements()) {
			name = keys.nextElement();
			value = table.get(name);
			b.append(URLEncoder.encode(name, encoding)).append(eq)
					.append(URLEncoder.encode(value, encoding));
			if (keys.hasMoreElements())
				b.append(amp);
		}
		return b;
	}

	public static StringBuilder encodeString(StringBuilder b,
			Hashtable<String, String> table)
			throws UnsupportedEncodingException {
		return encodeString(b, table, StringUtil.ENCODE_UTF_8);
	}

	public static String getBody(String url, String encoding)
			throws IOException, SocketTimeoutException {
		HttpURLConnection connection = (HttpURLConnection) new URL(url)
				.openConnection();
		try {
			if (connection == null) {
				throw new IOException("HttpURLConnection returns null.");
			}

			connection.setConnectTimeout(5000);
			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new IOException("HttpURLConnection responses bad result.");
			}

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					connection.getInputStream(), encoding));

			StringBuilder builder = new StringBuilder();
			String line = null;
			final char c = '\n';
			while ((line = reader.readLine()) != null) {
				builder.append(line).append(c);
			}
			reader.close();

			return builder.toString();
		} finally {
			connection.disconnect();
		}
	}

	public static String getBodyByPost(String url, String params, String encoding)
			throws IOException, SocketTimeoutException {
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
			
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(connection.getOutputStream()));
			pw.write(params);
			pw.flush();
			
			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new IOException("HttpURLConnection responses bad result.");
			}
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					connection.getInputStream(), encoding));

			StringBuilder builder = new StringBuilder();
			String line = null;
			final char c = '\n';
			while ((line = reader.readLine()) != null) {
				builder.append(line).append(c);
			}
			reader.close();

			return builder.toString();
		} finally {
			connection.disconnect();
		}
	}
	
	public static String getBody(String url) throws IOException,
			SocketTimeoutException {
		return getBody(url, StringUtil.ENCODE_UTF_8);
	}
}
