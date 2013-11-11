/*
 * Created on: Mar 6, 2012
 */
package com.intellij.plugin.reviewboard.entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

/**
 * @author George
 */
public class ReviewRequest {
    private String status;
    @SerializedName("last_updated")
    private String lastUpdated;
    private String description;
    @SerializedName("changedescription")
    private String changeDescription;
    private boolean published;
    @SerializedName("target_groups")
    private List<Group> groups;
    @SerializedName("bugs_closed")
    private List<Bug> bugs;
    @SerializedName("changenum")
    private String changeNum;
    @SerializedName("target_people")
    private List<People> people;
    @SerializedName("testing_done")
    private String testingDone;
    private String branch;
    @SerializedName("time_added")
    private String timeAdded;
    private String summary;
    private Long id;
    private Map<String, Link> links;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getChangeDescription() {
        return changeDescription;
    }

    public void setChangeDescription(String changeDescription) {
        this.changeDescription = changeDescription;
    }

    public Map<String, Link> getLinks() {
        return links;
    }

    public void setLinks(Map<String, Link> links) {
        this.links = links;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public List<Bug> getBugs() {
        return bugs;
    }

    public void setBugs(List<Bug> bugs) {
        this.bugs = bugs;
    }

    public String getChangeNum() {
        return changeNum;
    }

    public void setChangeNum(String changeNum) {
        this.changeNum = changeNum;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<People> getPeople() {
        return people;
    }

    public void setPeople(List<People> people) {
        this.people = people;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getTestingDone() {
        return testingDone;
    }

    public void setTestingDone(String testingDone) {
        this.testingDone = testingDone;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getTimeAdded() {
        return timeAdded;
    }

    public void setTimeAdded(String timeAdded) {
        this.timeAdded = timeAdded;
    }
}