/*
 * Created on: Mar 6, 2012
 */
package com.qiyi.reviewboard.entity;

/**
 *
 * @author Pragalathan M
 */
public class Bug {

    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Bug() {
    }

    public Bug(String text) {
        this.text = text;
    }
}
