/*
 * Created on: Mar 6, 2012
 */
package com.plugin.reviewboard.entity;

import com.google.gson.annotations.SerializedName;

/**
 * @author George
 */
public class ReviewRequestResponse extends ReviewBoardResponse {
    @SerializedName("review_request")
    private ReviewRequest reviewRequest;

    public ReviewRequest getReviewRequest() {
        return reviewRequest;
    }

    public void setReviewRequest(ReviewRequest reviewRequest) {
        this.reviewRequest = reviewRequest;
    }
}
