package me.wulfmarius.modinstaller.compatibility;

import java.util.Date;
import java.util.function.Predicate;

import me.wulfmarius.modinstaller.Version;

public class CompatibilityVersions {

    private CompatibilityVersion[] versions;

    public CompatibilityVersion floor(Date date) {
        return this.getMax(tldVersion -> tldVersion.getDate().compareTo(date) <= 0);
    }

    public CompatibilityVersion floor(Version version) {
        return this.getMax(tldVersion -> tldVersion.getParsedVersion().compareTo(version) <= 0);
    }

    public CompatibilityVersion[] getVersions() {
        return this.versions;
    }

    public void setVersions(CompatibilityVersion[] versions) {
        this.versions = versions;
    }

    private CompatibilityVersion getMax(Predicate<CompatibilityVersion> filter) {
        CompatibilityVersion result = null;

        for (CompatibilityVersion eachTldVersion : this.versions) {
            if (!filter.test(eachTldVersion)) {
                continue;
            }

            if (result == null || eachTldVersion.compareTo(result) > 0) {
                result = eachTldVersion;
            }
        }

        return result;
    }
}
