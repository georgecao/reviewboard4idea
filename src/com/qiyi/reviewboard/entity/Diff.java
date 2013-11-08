/*
 * Created on: Mar 6, 2012
 */
package com.qiyi.reviewboard.entity;

import java.util.Date;
import java.util.Map;

/**
 * @author George
 */
public class Diff {
    private Date timestamp;
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

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
