/*
 * Created on: Mar 6, 2012
 */
package com.qiyi.reviewboard.entity;

/**
 *
 * @author George
 */
public class UploadDiffResponse extends ReviewBoardResponse {

    private Diff diff;
    private String revision;
    private String file;

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public Diff getDiff() {
        return diff;
    }

    public void setDiff(Diff diff) {
        this.diff = diff;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }
}
