package com.plugin.reviewboard;

public class ReviewSettings {
    private String server;
    private String username;
    private String password;
    private String summary;
    private String description;
    private String changeDescription;
    private String branch;
    private String bugsClosed;
    private boolean testingDone;
    private String group;
    private String people;
    private Long reviewId;
    private String svnRoot;
    private String svnBasePath;
    private String diff;

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getBugsClosed() {
        return bugsClosed;
    }

    public void setBugsClosed(String bugsClosed) {
        this.bugsClosed = bugsClosed;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getPeople() {
        return people;
    }

    public void setPeople(String people) {
        this.people = people;
    }

    public Long getReviewId() {
        return reviewId;
    }

    public void setReviewId(Long reviewId) {
        this.reviewId = reviewId;
    }

    public String getSvnRoot() {
        return svnRoot;
    }

    public void setSvnRoot(String svnRoot) {
        this.svnRoot = svnRoot;
    }

    public String getSvnBasePath() {
        return svnBasePath;
    }

    public void setSvnBasePath(String svnBasePath) {
        this.svnBasePath = svnBasePath;
    }

    public String getDiff() {
        return diff;
    }

    public void setDiff(String diff) {
        this.diff = diff;
    }

    public boolean isTestingDone() {
        return testingDone;
    }

    public void setTestingDone(boolean testingDone) {
        this.testingDone = testingDone;
    }

    public String getChangeDescription() {
        return changeDescription;
    }

    public void setChangeDescription(String changeDescription) {
        this.changeDescription = changeDescription;
    }
}
