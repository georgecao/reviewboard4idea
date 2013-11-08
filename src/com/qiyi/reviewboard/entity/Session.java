/*
 * Created on: Mar 6, 2012
 */
package com.qiyi.reviewboard.entity;

import java.util.Map;

/**
 *
 * @author George
 */
public class Session {

    private boolean authenticated;
    private Map<String, Link> links;

    public Map<String, Link> getLinks() {
        return links;
    }

    public void setLinks(Map<String, Link> links) {
        this.links = links;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }
}
