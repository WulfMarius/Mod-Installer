package me.wulfmarius.modinstaller.update;

import java.util.Date;

import me.wulfmarius.modinstaller.repository.source.GithubAsset;

public class UpdateState {

    private String latestVersion;
    private String etag;
    private Date checked;
    private String releaseUrl;
    private GithubAsset asset;

    public GithubAsset getAsset() {
        return this.asset;
    }

    public Date getChecked() {
        return this.checked;
    }

    public String getEtag() {
        return this.etag;
    }

    public String getLatestVersion() {
        return this.latestVersion;
    }

    public String getReleaseUrl() {
        return this.releaseUrl;
    }

    public boolean hasAsset() {
        return this.asset != null;
    }

    public void setAsset(GithubAsset asset) {
        this.asset = asset;
    }

    public void setChecked(Date checked) {
        this.checked = checked;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public void setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
    }

    public void setReleaseUrl(String releaseUrl) {
        this.releaseUrl = releaseUrl;
    }
}
