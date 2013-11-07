/**
 *
 * History:
 *   11-5-15 8:39 Pm Created by ZGong
 */
package rb;

import com.google.gson.Gson;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.ui.Messages;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Created by ZGong.
 *
 * @version 1.0 11-5-15 8:39 Pm
 */
public class ReviewBoardClient {

    private static Long startTime = System.currentTimeMillis();
    String apiUrl;

    ReviewBoardClient(String server, final String username, final String password) {
        this.apiUrl = server + "/api/";
        class MyAuthenticator extends Authenticator {
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password.toCharArray());
            }
        }
        FileIOUtils.fileWrite("apiUrl" + apiUrl, "LOG.txt", startTime);
        Authenticator.setDefault(new MyAuthenticator());
    }

    public static boolean postReview(ReviewSettings settings,
                                     final ProgressIndicator progressIndicator) {
        FileIOUtils.fileWrite("postReview is ", "LOG.txt", startTime);
        ReviewBoardClient reviewBoardClient =
                new ReviewBoardClient(settings.getServer(), settings.getUsername(), settings.getPassword());
        try {
            String reviewId = settings.getReviewId();
            if (reviewId == null || "".equals(reviewId)) {
                FileIOUtils.fileWrite("Creating review draft...", "LOG.txt", startTime);
                progressIndicator.setText("Creating review draft...");
                NewReviewResponse newRequest = null;
                try {
                    newRequest = reviewBoardClient.createNewRequest(settings.getSvnRoot());
                } catch (Exception exception) {
                    FileIOUtils.fileWrite("Received a " + exception + " while creating a new request",
                            "LOG.txt",
                            startTime);
                    FileIOUtils.fileWrite(
                            "settings " + settings.getServer() + ", "
                                    + settings.getSvnBasePath() + ", "
                                    + settings.getSvnRoot() + ", "
                                    + settings.getDiff(), "LOG.txt", startTime);
                    // There is a very good chance that we received a 400, since
                    // the SVNRoot we
                    // passed may not match what is configured on the server.
                    // Now try to
                    // recreate the SVN settings based on what is configured on
                    // the server and retry.
                    if (reviewBoardClient.updateSVNAttributes(settings)) { // side-effect
                        FileIOUtils.fileWrite("Retrying the request with svnroot " + settings.getSvnRoot(),
                                "LOG.txt",
                                startTime);
                        System.out.println("Retrying the request with svnroot : " + settings.getSvnRoot());
                        newRequest = reviewBoardClient.createNewRequest(settings.getSvnRoot());
                        FileIOUtils.fileWrite("newRequest is " + newRequest, "LOG.txt", startTime);
                    }
                }
                FileIOUtils.fileWrite("Create new request:"
                        + newRequest.review_request, "LOG.txt", startTime);
                progressIndicator.setText("Create new request:"
                        + newRequest.review_request);
                System.out.println(newRequest.review_request.id);
                reviewId = newRequest.review_request.id;
                FileIOUtils.fileWrite("ReviewId:" + reviewId, "LOG.txt",
                        startTime);
                settings.setReviewId(reviewId);
            }
            progressIndicator.setText("Updating draft...");
            DraftResponse response = reviewBoardClient.updateDraft(reviewId,
                    settings);
            if (!response.isOk()) {
                FileIOUtils.fileWrite("response:" + response, "LOG.txt",
                        startTime);
                final Response finalResponse = response;
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        FileIOUtils
                                .fileWrite("runId : 1", "LOG.txt", startTime);
                        JOptionPane.showMessageDialog(null, finalResponse.err,
                                "Warning", JOptionPane.WARNING_MESSAGE);
                    }
                });
                return false;
            }
            response = reviewBoardClient.updateDraftWithLimitField(reviewId,
                    settings);

            if (!response.isOk()) {
                progressIndicator.setText(response.err);
            }
            progressIndicator.setText("Draft is updated");
            progressIndicator.setText("Uploading diff...");
            final Response diff = reviewBoardClient.diff(reviewId,
                    settings.getSvnBasePath(), settings.getDiff());
            if (!diff.isOk()) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        FileIOUtils
                                .fileWrite("runId : 2", "LOG.txt", startTime);
                        JOptionPane.showMessageDialog(null, diff.err,
                                "Warning", JOptionPane.WARNING_MESSAGE);
                    }
                });
                return false;
            }
            progressIndicator.setText("Diff is updated");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public static void loadReview(ReviewSettings settings, String reviewId)
            throws Exception {
        ReviewBoardClient rb = new ReviewBoardClient(settings.getServer(),
                settings.getUsername(), settings.getPassword());
        ReviewRequest reviewRequest;
        try {
            DraftResponse reviewInfo = rb.getReviewInfoAsDraft(reviewId);
            reviewRequest = reviewInfo.draft;
        } catch (Exception e) {
            NewReviewResponse reviewInfoAsPublished = rb
                    .getReviewInfoAsPublished(reviewId);
            reviewRequest = reviewInfoAsPublished.review_request;
        }

        if (reviewRequest != null) {
            settings.setBranch(reviewRequest.branch);
            settings.setDescription(reviewRequest.description);
            settings.setBugsClosed(reviewRequest.getBugs_closed());
            settings.setPeople(reviewRequest.getTarget_people());
            settings.setGroup(reviewRequest.getTarget_group());
        } else {
            Messages.showErrorDialog("No such review id:" + reviewId, "Error");
        }
    }

    private boolean updateSVNAttributes(ReviewSettings settings) {
        try {
            RepositoriesResponse repositoriesResponse = getRepositories();
            FileIOUtils.fileWrite("repositoriesResponse is "
                    + repositoriesResponse.total_results
                    + repositoriesResponse.repositories.length, "LOG.txt",
                    startTime);
            if (repositoriesResponse.isOk()) {
                String svnPath = settings.getSvnRoot()
                        + settings.getSvnBasePath();
                FileIOUtils.fileWrite("svnPath is " + svnPath, "LOG.txt",
                        startTime);
                for (Repository repo : repositoriesResponse.repositories) {
                    FileIOUtils.fileWrite("svnPath is " + svnPath, "LOG.txt",
                            startTime);
                    FileIOUtils.fileWrite("repo.path is " + repo.path, "LOG.txt",
                            startTime);
                    if (svnPath.startsWith(repo.path)) {
                        settings.setSvnRoot(repo.path);
                        FileIOUtils.fileWrite("repo.path.length() is " + repo.path
                                .length(),
                                "LOG.txt", startTime);
                        String svnBasePath = svnPath.substring(0, repo.path
                                .length());
                        settings.setSvnBasePath(svnBasePath);
                        FileIOUtils.fileWrite("svnBasePath is " + svnBasePath,
                                "LOG.txt", startTime);
                        FileIOUtils.fileWrite("svnRoot is " + repo.path,
                                "LOG.txt", startTime);
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            FileIOUtils
                    .fileWrite(
                            "Error while getting the repositories list from the server.",
                            "LOG.txt", startTime);
            System.out
                    .println("Error while getting the repositories list from the server.");
        }
        return false;
    }

    public DraftResponse updateDraft(String reviewId, ReviewSettings settings)
            throws Exception {
        String path = String.format("review-requests/%s/draft/", reviewId);
        Map<String, Object> params = new HashMap<String, Object>();
        addParams(params, "summary", settings.getSummary());
        addParams(params, "branch", settings.getBranch());
        addParams(params, "bugs_closed", settings.getBugsClosed());
        addParams(params, "description", settings.getDescription());
        String json = new HttpClient().httpPut(path, params);
        FileIOUtils.fileWrite("path is " + path, "LOG.txt", startTime);
        FileIOUtils.fileWrite("updateDraft() is " + json, "LOG.txt", startTime);
        System.out.println(json);
        Gson gson = new Gson();
        return gson.fromJson(json, DraftResponse.class);
    }

    public DraftResponse updateDraftWithLimitField(String reviewId,
                                                   ReviewSettings settings) throws Exception {
        String path = String.format("review-requests/%s/draft/", reviewId);
        Map<String, Object> params = new HashMap<String, Object>();
        addParams(params, "target_groups", settings.getGroup());
        addParams(params, "target_people", settings.getPeople());
        String json;
        try {
            json = new HttpClient().httpPut(path, params);
        } catch (Exception e) {
            return new DraftResponse("error", "Invalid group or people");
        }
        FileIOUtils.fileWrite("path is " + path, "LOG.txt", startTime);
        FileIOUtils.fileWrite("updateDraftWithLimitField() is " + json,
                "LOG.txt", startTime);
        System.out.println(json);
        Gson gson = new Gson();
        return gson.fromJson(json, DraftResponse.class);
    }

    private void addParams(Map<String, Object> params, String key, String value) {
        if (value != null && !"".equals(value)) {
            params.put(key, value);
        }
    }

    public NewReviewResponse createNewRequest(String repository)
            throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("repository", repository);
        String json = new HttpClient().httpPost("review-requests/", params);
        System.out.println(json);
        FileIOUtils.fileWrite("createNewRequest() is " + json, "LOG.txt",
                startTime);
        Gson gson = new Gson();
        return gson.fromJson(json, NewReviewResponse.class);
    }

    public Response diff(String reviewId, String basedir, String diff)
            throws Exception {
        String path = String.format("review-requests/%s/diffs/", reviewId);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("basedir", basedir);
        params.put("path", new MemoryFile("review.diff", diff));
        FileIOUtils.fileWrite("path is " + path, "LOG.txt", startTime);
        FileIOUtils.fileWrite("basedir is " + basedir, "LOG.txt", startTime);
        String json = new HttpClient().httpPost(path, params);
        System.out.println(json);
        FileIOUtils.fileWrite("diff() is " + json, "LOG.txt", startTime);
        Gson gson = new Gson();
        return gson.fromJson(json, Response.class);
    }

    public DraftResponse getReviewInfoAsDraft(String reviewId) throws Exception {
        String path = String.format("review-requests/%s/draft/", reviewId);
        String json = new HttpClient().httpGet(path);
        System.out.println(json);
        FileIOUtils.fileWrite("path is " + path, "LOG.txt", startTime);
        FileIOUtils.fileWrite("getReviewInfoAsDraft() is " + json, "LOG.txt",
                startTime);
        Gson gson = new Gson();
        return gson.fromJson(json, DraftResponse.class);
    }

    public NewReviewResponse getReviewInfoAsPublished(String reviewId)
            throws Exception {
        String path = String.format("review-requests/%s/", reviewId);
        String json = new HttpClient().httpGet(path);
        System.out.println(json);
        FileIOUtils.fileWrite("path is " + path, "LOG.txt", startTime);
        FileIOUtils.fileWrite("getReviewInfoAsPublished() is " + json,
                "LOG.txt", startTime);
        Gson gson = new Gson();
        return gson.fromJson(json, NewReviewResponse.class);
    }

    public RepositoriesResponse getRepositories() throws Exception {
        String path = "repositories/";
        String json = new HttpClient().httpGet(path);
        System.out.println(json);
        FileIOUtils.fileWrite("path is " + path, "LOG.txt", startTime);
        FileIOUtils.fileWrite("getRepositories() is " + json, "LOG.txt",
                startTime);
        Gson gson = new Gson();
        return gson.fromJson(json, RepositoriesResponse.class);
    }

    class HttpClient {
        public String httpGet(String path) throws Exception {
            URL url = new URL(apiUrl + path);
            System.out.println("Http get:" + url);
            URLConnection urlConnection = url.openConnection();
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    inputStream));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }

        public String httpRequest(String path, String method,
                                  Map<String, String> params) throws Exception {
            URL url = new URL(apiUrl + path);
            System.out.println("Http " + method + ":" + url);
            URLConnection urlConnection = url.openConnection();
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            HttpURLConnection http = (HttpURLConnection) urlConnection;
            http.setRequestMethod(method);
            OutputStream outputStream = urlConnection.getOutputStream();
            PrintWriter pw = new PrintWriter(outputStream, true);
            for (String key : params.keySet()) {
                pw.println(key + "=" + params.get(key));
            }
            pw.flush();
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    inputStream));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }

        public String httpRequestWithMultiplePart(String path, String method,
                                                  Map<String, Object> params) throws Exception {
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
            Iterator iter = set.iterator();
            while (iter.hasNext()) {
                String str = iter.next().toString();
                FileIOUtils.fileWrite(str, "LOG.txt", startTime);
            }
            InputStream inputStream = chr.post(params);
            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    inputStream));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }

        public String httpPost(String path, Map<String, Object> params)
                throws Exception {
            return httpRequestWithMultiplePart(path, "POST", params);
        }

        private String httpPut(String path, Map<String, Object> params)
                throws Exception {
            return httpRequestWithMultiplePart(path, "PUT", params);
        }
    }
}

class Href {
    String href;
    String method;

    @Override
    public String toString() {
        return "Href{" + "href='" + href + '\'' + ", method='" + method + '\''
                + '}';
    }
}

class Response {
    public static final Response OK = new Response("ok");
    public static final Response ERROR = new Response("error");
    String stat;
    String err;

    Response() {
    }

    Response(String stat) {
        this.stat = stat;
    }

    public Response(String stat, String err) {
        this.stat = stat;
        this.err = err;
    }

    public boolean isOk() {
        return "ok".equals(stat);
    }
}

class People extends Href {
    String title;
}

class Group extends Href {
    String title;
}

class ReviewRequest {
    String status;
    String id;
    String last_updated;
    String description;
    Map<String, Href> links;
    String[] bugs_closed;
    String summary;
    String branch;
    People[] target_people;
    Group[] target_groups;

    public String getBugs_closed() {
        StringBuilder sb = new StringBuilder();
        for (String s : bugs_closed) {
            sb.append(s).append(",");
        }
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == ',') {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    private <T> String join(T[] t, char c, Transform<T> cal) {
        StringBuilder sb = new StringBuilder();
        for (T o : t) {
            sb.append(cal.transform(o)).append(c);
        }
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == ',') {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    public String getTarget_people() {
        return join(target_people, ',', new Transform<People>() {
            @Override
            public String transform(People people) {
                return people.title;
            }
        });
    }

    public String getTarget_group() {
        return join(target_groups, ',', new Transform<Group>() {
            @Override
            public String transform(Group group) {
                return group.title;
            }
        });
    }

    @Override
    public String toString() {
        return "ReviewRequest{" + "status='" + status + '\'' + ", id='" + id
                + '\'' + ", last_updated='" + last_updated + '\''
                + ", description='" + description + '\'' + ", links=" + links
                + ", bugs_closed='" + bugs_closed + '\'' + ", summary='"
                + summary + '\'' + ", branch='" + branch + '\''
                + ", target_people="
                + (target_people == null ? null : Arrays.asList(target_people))
                + ", target_groups="
                + (target_groups == null ? null : Arrays.asList(target_groups))
                + '}';
    }

    interface Transform<T> {
        public String transform(T t);
    }
}

class NewReviewResponse extends Response {
    ReviewRequest review_request;

    @Override
    public String toString() {
        return "NewReviewResponse{" + "stat='" + stat + '\''
                + ", review_request=" + review_request + '}';
    }
}

class DraftResponse extends Response {
    ReviewRequest draft;

    DraftResponse(String stat, String err) {
        super(stat, err);
    }

    @Override
    public String toString() {
        return "DraftResponse{" + "draft=" + draft + '}';
    }
}

/**
 * Repository information returned by the RB server.
 *
 * @see-also http://www.reviewboard.org/docs/manual/1.7/webapi/2.0/resources/repository
 * -list/ { "id": 1, "links": { "delete": { "href":
 * "http://reviews.example.com/api/repositories/1/", "method":
 * "DELETE" }, "info": { "href":
 * "http://reviews.example.com/api/repositories/1/info/", "method":
 * "GET" }, "self": { "href":
 * "http://reviews.example.com/api/repositories/1/", "method": "GET"
 * }, "update": { "href":
 * "http://reviews.example.com/api/repositories/1/", "method": "PUT" }
 * }, "name": "Review Board SVN", "path":
 * "http://reviewboard.googlecode.com/svn", "tool": "Subversion" }
 */
class Repository {
    String id;
    Map<String, Href> links;
    String name; // name of the repository
    String path; // repository path or root
    String tool;

}

class RepositoriesResponse extends Response {
    Map<String, Href> links;
    Repository[] repositories;
    Integer total_results;
}