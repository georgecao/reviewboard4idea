package rb;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Say it.
 *
 * @author george
 * @since 11/7/13 3:29 PM
 */

public class HttpClient {
    String apiUrl;
    Long startTime;
    String UTF_8 = "UTF-8";

    public HttpClient(String apiUrl, Long startTime) {
        this.apiUrl = apiUrl;
        this.startTime = startTime;
    }

    public static HttpClient create(String apiUrl, Long startTime) {
        return new HttpClient(apiUrl, startTime);
    }

    /**
     * Simple tests.
     */
    public static void main(String... args) throws UnsupportedEncodingException {
        class MyAuthenticator extends Authenticator {
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("caozhangzhi", "9648iqiyi*COM".toCharArray());
            }
        }
        Authenticator.setDefault(new MyAuthenticator());
        String val = "https://scm.qiyi.domain:18080/svn/shreark/vrs";
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("repository", val);
        String path = "review-requests/";
        HttpClient client = HttpClient.create("http://reviewboard.qiyi.domain/api/", System.currentTimeMillis());
        try {
            String response = client.post(path, params);
            System.out.println(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String get(String path) throws Exception {
        URL url = new URL(apiUrl + path);
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

    private String append(String path, Map<String, Object> params) throws UnsupportedEncodingException {
        if (null == params || params.size() == 0) {
            return path;
        }
        StringBuilder sb = new StringBuilder(path).append("?");
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            sb.append(entry.getKey())
                    .append("=")
                    .append(URLEncoder.encode(entry.getValue().toString(), UTF_8))
                    .append("&");
        }
        return sb.toString();
    }

    public String request(String path, String method, Map<String, Object> params) throws Exception {
        URL url = new URL(apiUrl + append(path, params));
        System.out.println("Http " + method + ":" + url);
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setDoInput(true);
        http.setDoOutput(false);
        http.setRequestMethod(method);
        http.setRequestProperty("Accept", "application/json");
        InputStream inputStream = http.getInputStream();
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        close(inputStream);
        return sb.toString();
    }

    public String requestWithMultiPart(String path, String method, Map<String, Object> params) throws Exception {
        URL url = new URL(apiUrl + path);
        System.out.println("Http " + method + ":" + url);
        FileIOUtils.fileWrite("url is : " + url, "LOG.txt", startTime);
        URLConnection urlConnection = url.openConnection();
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);
        HttpURLConnection http = (HttpURLConnection) urlConnection;
        http.setRequestMethod(method);
        ClientHttpRequest chr = new ClientHttpRequest(http);
        Set set = params.keySet();
        Iterator iterator = set.iterator();
        while (iterator.hasNext()) {
            String str = iterator.next().toString();
            FileIOUtils.fileWrite(str, "LOG.txt", startTime);
        }
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

    public String post(String path, Map<String, Object> params) throws Exception {
        return request(path, "POST", params);
    }

    public String postWithMultiPart(String path, Map<String, Object> params) throws Exception {
        return requestWithMultiPart(path, "POST", params);
    }

    public String put(String path, Map<String, Object> params) throws Exception {
        return requestWithMultiPart(path, "PUT", params);
    }

    private void close(Closeable... resources) throws IOException {
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
