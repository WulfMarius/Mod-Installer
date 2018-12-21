package me.wulfmarius.modinstaller.compatibility;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

import me.wulfmarius.modinstaller.Version;

public class CompatibilityVersion implements Comparable<CompatibilityVersion> {

    private String version;
    private Date date;

    @JsonIgnore
    private transient Version parsedVersion;

    @Override
    public int compareTo(CompatibilityVersion result) {
        return this.getParsedVersion().compareTo(result.getParsedVersion());
    }

    public Date getDate() {
        return this.date;
    }

    public Version getParsedVersion() {
        if (this.parsedVersion == null) {
            this.parsedVersion = Version.parse(this.version);
        }

        return this.parsedVersion;
    }

    public String getVersion() {
        return this.version;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
