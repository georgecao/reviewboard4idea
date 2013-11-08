/*
 * Created on: Mar 6, 2012
 */
package com.qiyi.reviewboard.entity;

import com.google.gson.annotations.SerializedName;

/**
 * @author Pragalathan M
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
