package me.wulfmarius.modinstaller.repository.source;

import com.fasterxml.jackson.annotation.*;

import me.wulfmarius.modinstaller.Asset;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubAsset {

    private String name;

    @JsonProperty("browser_download_url")
    private String downloadUrl;

    public String getDownloadUrl() {
        return this.downloadUrl;
    }

    public String getName() {
        return this.name;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Asset toAsset() {
        return Asset.withUrl(this.downloadUrl);
    }
}
