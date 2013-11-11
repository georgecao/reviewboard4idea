/*
 * Created on: Mar 6, 2012
 */
package com.plugin.reviewboard.entity;


import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

/**
 * @author George
 */
public class RepositoryResponse extends ReviewBoardResponse {
    @SerializedName("total_results")
    private int totalResults;
    private List<Repository> repositories;
    private Map<String, Link> links;

    public Map<String, Link> getLinks() {
        return links;
    }

    public void setLinks(Map<String, Link> links) {
        this.links = links;
    }

    public List<Repository> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<Repository> repositories) {
        this.repositories = repositories;
    }

    public int getTotalResults() {

        return totalResults;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }
}
