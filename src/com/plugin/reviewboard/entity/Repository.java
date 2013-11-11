/*
 * Created on: Mar 6, 2012
 */
package com.plugin.reviewboard.entity;

import java.util.Map;

/**
 * @author George
 */
public class Repository {
    private String path;
    private String tool;
    private Long id;
    private String name;
    private Map<String, Link> links;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTool() {
        return tool;
    }

    public void setTool(String tool) {
        this.tool = tool;
    }
}
