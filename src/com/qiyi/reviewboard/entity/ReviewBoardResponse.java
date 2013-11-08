/*
 * Created on: Mar 6, 2012
 */
package com.qiyi.reviewboard.entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

/**
 * @author George
 */
public class ReviewBoardResponse {
    @SerializedName("fields")
    private Map<String, List<String>> failedFields;
    private Error err;
    private String stat;

    public ReviewBoardResponse() {
    }

    public Error getErr() {
        return err;
    }

    public void setErr(Error err) {
        this.err = err;
    }

    public Map<String, List<String>> getFailedFields() {
        return failedFields;
    }

    public void setFailedFields(Map<String, List<String>> failedFields) {
        this.failedFields = failedFields;
    }

    public String getStat() {
        return stat;
    }

    public void setStat(String stat) {
        this.stat = stat;
    }

    public boolean isSuccessful() {
        return "ok".equals(stat);
    }
}
