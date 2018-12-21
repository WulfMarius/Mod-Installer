package me.wulfmarius.modinstaller.compatibility;

import java.util.Date;

public class CompatibilityState {

    private CompatibilityVersions compatibilityVersions;
    private String etag;
    private Date checked;

    public Date getChecked() {
        return this.checked;
    }

    public CompatibilityVersions getCompatibilityVersions() {
        return this.compatibilityVersions;
    }

    public String getEtag() {
        return this.etag;
    }

    public void setChecked(Date checked) {
        this.checked = checked;
    }

    public void setCompatibilityVersions(CompatibilityVersions compatibilityVersions) {
        this.compatibilityVersions = compatibilityVersions;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }
}
