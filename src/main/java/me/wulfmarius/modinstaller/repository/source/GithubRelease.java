package me.wulfmarius.modinstaller.repository.source;

import java.util.Date;

import com.fasterxml.jackson.annotation.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubRelease {

    private String name;

    @JsonProperty("html_url")
    private String url;

    @JsonProperty("published_at")
    private Date date;

    private GithubAsset[] assets;

    private String body;

    public GithubAsset[] getAssets() {
        return this.assets;
    }

    public String getBody() {
        return this.body;
    }

    public Date getDate() {
        return this.date;
    }

    public String getName() {
        return this.name;
    }

    public String getUrl() {
        return this.url;
    }

    public void setAssets(GithubAsset[] assets) {
        this.assets = assets;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
