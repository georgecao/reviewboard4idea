package com.qiyi.reviewboard.client;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.ui.Messages;
import com.qiyi.reviewboard.ReviewSettings;
import com.qiyi.reviewboard.entity.*;
import com.qiyi.reviewboard.http.HttpClient;

import javax.swing.*;
import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.HashMap;
import java.util.Map;

public class ReviewBoardClient {
    String apiUrl;

    ReviewBoardClient(String server, final String username, final String password) {
        this.apiUrl = server + "/api/";
        class MyAuthenticator extends Authenticator {
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password.toCharArray());
            }
        }
        Authenticator.setDefault(new MyAuthenticator());
    }

    public static boolean postReview(ReviewSettings settings,
                                     final ProgressIndicator progressIndicator) {
        ReviewBoardClient reviewBoardClient
                = new ReviewBoardClient(settings.getServer(), settings.getUsername(), settings.getPassword());
        try {
            String reviewId = settings.getReviewId();
            if (Strings.isNullOrEmpty(reviewId)) {
                progressIndicator.setText("Creating review draft...");
                Repository repo = reviewBoardClient.findThenRefresh(settings);
                ReviewRequest newRequest = reviewBoardClient.createReviewRequest(repo);
                progressIndicator.setText("Create new request:" + newRequest.getId());
                reviewId = String.valueOf(newRequest.getId());
                settings.setReviewId(reviewId);
            }
            progressIndicator.setText("Updating draft...");
            final DraftResponse response = reviewBoardClient.updateDraft(reviewId, settings);
            if (!response.isSuccessful()) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        JOptionPane.showMessageDialog(null, response.getErr(), "Warning", JOptionPane.WARNING_MESSAGE);
                    }
                });
                return false;
            }
            progressIndicator.setText("Draft is updated");
            progressIndicator.setText("Uploading diff...");
            String baseDir = settings.getSvnRoot() + settings.getSvnBasePath();
            final UploadDiffResponse diff = reviewBoardClient.uploadDiff(reviewId, baseDir, settings.getDiff());
            if (!diff.isSuccessful()) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        JOptionPane.showMessageDialog(null, diff.getErr(),
                                "Warning", JOptionPane.WARNING_MESSAGE);
                    }
                });
                return false;
            }
            progressIndicator.setText("Diff is uploaded.");
            reviewBoardClient.publish(reviewId);
            progressIndicator.setText("Review request is published.");
        } catch (IOException e) {
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
            reviewRequest = reviewInfo.getDraft();
        } catch (Exception e) {
            ReviewRequestResponse reviewInfoAsPublished = rb.getReviewInfoAsPublished(reviewId);
            reviewRequest = reviewInfoAsPublished.getReviewRequest();
        }
        if (reviewRequest != null) {
            settings.setBranch(reviewRequest.getBranch());
            settings.setDescription(reviewRequest.getDescription());
            settings.setBugsClosed(ParamUtils.join(reviewRequest.getBugs(), new Function<Bug, String>() {
                @Override
                public String apply(Bug bug) {
                    return bug.getText();
                }
            }));
            settings.setPeople(ParamUtils.join(reviewRequest.getPeople(), new Function<People, String>() {
                @Override
                public String apply(People people) {
                    return people.getTitle();
                }
            }));
            settings.setGroup(ParamUtils.join(reviewRequest.getGroups(), new Function<Group, String>() {
                @Override
                public String apply(Group group) {
                    return group.getTitle();
                }
            }));
        } else {
            Messages.showErrorDialog("No such review id:" + reviewId, "Error");
        }
    }

    /**
     * Find the best repository matching the current svn settings.
     * There is a very good chance that we received a 400, since the SVNRoot we passed may not match what is configured on the server.
     * Now try to recreate the SVN settings based on what is configured on the server and retry.
     */
    private Repository findThenRefresh(ReviewSettings settings) {
        Repository repo = match(settings.getSvnRoot() + settings.getSvnBasePath());
        refresh(repo, settings);
        return repo;
    }

    private Repository match(String svnPath) {
        try {
            RepositoryResponse repositoriesResponse = getRepositories();
            if (repositoriesResponse.isSuccessful()) {
                for (Repository repo : repositoriesResponse.getRepositories()) {
                    if (svnPath.startsWith(repo.getPath())) {
                        return repo;
                    }
                }
            }
        } catch (IOException e) {

        }
        return null;
    }

    private boolean refresh(Repository repo, ReviewSettings settings) {
        String svnPath = settings.getSvnRoot() + settings.getSvnBasePath();
        if (null != repo) {
            settings.setSvnRoot(repo.getPath());
            String svnBasePath = svnPath.substring(repo.getPath().length());
            settings.setSvnBasePath(svnBasePath);
            return true;
        }
        return false;
    }

    public DraftResponse updateDraft(String reviewId, ReviewSettings settings) throws IOException {
        String path = url(String.format("review-requests/%s/draft/", reviewId));
        Map<String, Object> params = new HashMap<String, Object>();
        addParams(params, "summary", settings.getSummary());
        addParams(params, "branch", settings.getBranch());
        addParams(params, "bugs_closed", settings.getBugsClosed());
        addParams(params, "description", settings.getDescription());
        addParams(params, "target_groups", settings.getGroup());
        addParams(params, "target_people", settings.getPeople());
        return HttpClient.put(path, params, DraftResponse.class);
    }

    public DraftResponse publish(String reviewId) throws IOException {
        String path = url(String.format("review-requests/%s/draft/", reviewId));
        Map<String, Object> params = new HashMap<String, Object>(1);
        addParams(params, "public", "true");
        return HttpClient.put(path, params, DraftResponse.class);
    }

    private void addParams(Map<String, Object> params, String key, String value) {
        if (Strings.isNullOrEmpty(value)) {
            return;
        }
        params.put(key, value);
    }

    public ReviewRequest createReviewRequest(Repository repository) throws IOException {
        if (repository == null) {
            return null;
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("repository", repository.getName());
        ReviewRequestResponse response
                = HttpClient.post(url("review-requests/"), params, ReviewRequestResponse.class);
        return null == response ? null : response.getReviewRequest();
    }

    public UploadDiffResponse uploadDiff(String reviewId, String basedir, String diff) throws IOException {
        String path = url(String.format("review-requests/%s/diffs/", reviewId));
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("basedir", basedir);
        params.put("path", new MemoryFile("review.diff", diff));
        UploadDiffResponse response = HttpClient.postWithMultiPart(path, params, UploadDiffResponse.class);
        return response;
    }

    public DraftResponse getReviewInfoAsDraft(String reviewId) throws Exception {
        String path = url(String.format("review-requests/%s/draft/", reviewId));
        return HttpClient.get(path, DraftResponse.class);
    }

    private String url(String path) {
        return apiUrl + path;
    }

    public ReviewRequestResponse getReviewInfoAsPublished(String reviewId) throws IOException {
        String path = url(String.format("review-requests/%s/", reviewId));
        return HttpClient.get(path, ReviewRequestResponse.class);
    }

    public RepositoryResponse getRepositories() throws IOException {
        String path = url("repositories/");
        return HttpClient.get(path, RepositoryResponse.class);
    }
}