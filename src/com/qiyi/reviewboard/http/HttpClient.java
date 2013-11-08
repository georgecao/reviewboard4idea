package com.qiyi.reviewboard.http;

import com.google.gson.Gson;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Say it.
 *
 * @author george
 * @since 11/7/13 3:29 PM
 */

public class HttpClient {
    static final String UTF_8 = "UTF-8";

    public static HttpClient create() {
        return new HttpClient();
    }

    private static byte[] content(Map<String, Object> params) throws UnsupportedEncodingException {
        if (null == params || params.size() == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            sb.append(entry.getKey())
                    .append("=")
                    .append(URLEncoder.encode(entry.getValue().toString(), UTF_8))
                    .append("&");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString().getBytes(UTF_8);
    }

    public static String request(String path, String method, Map<String, Object> params) throws IOException {
        URL url = new URL(path);
        System.out.println("Http " + method + ":" + url);
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setDoInput(true);
        http.setDoOutput(true);
        http.setRequestMethod(method);
        http.setRequestProperty("Accept", "application/json");
        byte[] content = content(params);
        if (null != content) {
            http.setRequestProperty("Content-Length", String.valueOf(content.length));
            OutputStream out = http.getOutputStream();
            out.write(content);
            out.flush();
        }
        int code = http.getResponseCode();
        InputStream in;
        if (code >= 400) {
            in = http.getErrorStream();
        } else {
            in = http.getInputStream();
        }
        String response = readStream(in);
        close(in);
        return response;
    }

    public static <T> T post(String path, Map<String, Object> params, Class<T> response) throws IOException {
        String content = post(path, params);
        Gson gson = new Gson();
        return gson.fromJson(content, response);
    }

    public static String post(String path, Map<String, Object> params) throws IOException {
        return request(path, "POST", params);
    }

    public static <T> T get(String path, Class<T> clazz) throws IOException {
        String content = get(path);
        Gson gson = new Gson();
        return gson.fromJson(content, clazz);
    }

    public static String get(String path) throws IOException {
        URL url = new URL(path);
        System.out.println("Http get:" + url);
        URLConnection urlConnection = url.openConnection();
        InputStream inputStream = urlConnection.getInputStream();
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        close(inputStream);
        return sb.toString();
    }

    private static String readStream(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    public static String requestWithMultiPart(String path, String method, Map<String, Object> params) throws IOException {
        URL url = new URL(path);
        URLConnection urlConnection = url.openConnection();
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);
        HttpURLConnection http = (HttpURLConnection) urlConnection;
        http.setRequestMethod(method);
        ClientHttpRequest chr = new ClientHttpRequest(http);
        InputStream inputStream = chr.post(params);
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        close(inputStream);
        return sb.toString();
    }

    public static <T> T postWithMultiPart(String path, Map<String, Object> params, Class<T> response) throws IOException {
        String content = requestWithMultiPart(path, "POST", params);
        Gson gson = new Gson();
        return gson.fromJson(content, response);
    }

    public static <T> T put(String path, Map<String, Object> params, Class<T> response) throws IOException {
        String content = requestWithMultiPart(path, "PUT", params);
        Gson gson = new Gson();
        return gson.fromJson(content, response);
    }

    private static void close(Closeable... resources) throws IOException {
        if (null == resources) {
            return;
        }
        for (Closeable resource : resources) {
            if (null != resource) {
                resource.close();
            }
        }
    }
}
