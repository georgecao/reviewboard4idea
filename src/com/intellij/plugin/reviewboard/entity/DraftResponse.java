/*
 * Created on: Mar 6, 2012
 */
package com.intellij.plugin.reviewboard.entity;

/**
 * @author George
 */
public class DraftResponse extends ReviewBoardResponse {

    private ReviewRequest draft;

    public ReviewRequest getDraft() {
        return draft;
    }

    public void setDraft(ReviewRequest draft) {
        this.draft = draft;
    }
}
