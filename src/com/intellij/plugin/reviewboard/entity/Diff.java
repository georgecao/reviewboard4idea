/*
 * Created on: Mar 6, 2012
 */
package com.intellij.plugin.reviewboard.entity;

import java.util.Map;

/**
 * @author George
 */
public class Diff {
    private String timestamp;
    private String name;
    private Long id;
    private Long revision;
    private Map<String, Link> links;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Map<String, Link> getLinks() {
        return links;
    }

    public void setLinks(Map<String, Link> links) {
        this.links = links;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getRevision() {
        return revision;
    }

    public void setRevision(Long revision) {
        this.revision = revision;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
