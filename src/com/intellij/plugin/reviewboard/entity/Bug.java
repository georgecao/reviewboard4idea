/*
 * Created on: Mar 6, 2012
 */
package com.intellij.plugin.reviewboard.entity;

/**
 * @author George
 */
public class Bug {

    private String text;

    public Bug() {
    }

    public Bug(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
