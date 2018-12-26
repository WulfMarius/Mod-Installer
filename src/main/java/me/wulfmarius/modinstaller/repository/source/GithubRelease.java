package me.wulfmarius.modinstaller.repository.source;

import java.util.Date;

import com.fasterxml.jackson.annotation.*;

import me.wulfmarius.modinstaller.Version;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubRelease {

    private String name;

    @JsonProperty("tag_name")
    private String tag;

    @JsonProperty("html_url")
    private String url;

    @JsonProperty("published_at")
    private Date date;

    private GithubAsset[] assets;

    private GithubAuthor author;

    private String body;

    public GithubAsset[] getAssets() {
        return this.assets;
    }

    public GithubAuthor getAuthor() {
        return this.author;
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

    public String getTag() {
        return this.tag;
    }

    public String getUrl() {
        return this.url;
    }

    public boolean hasMatchingTag(String tagName) {
        if (this.tag == null) {
            return false;
        }

        if (this.tag.equalsIgnoreCase(tagName)) {
            return true;
        }

        try {
            return Version.parse(this.tag).equals(Version.parse(tagName));
        } catch (Exception e) {
            return false;
        }
    }

    public void setAssets(GithubAsset[] assets) {
        this.assets = assets;
    }

    public void setAuthor(GithubAuthor author) {
        this.author = author;
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

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
