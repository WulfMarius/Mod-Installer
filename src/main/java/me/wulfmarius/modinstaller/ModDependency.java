package me.wulfmarius.modinstaller;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ModDependency {

    private String name;
    private String version;

    @JsonIgnore
    private transient VersionRequirement versionRequirement;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ModDependency)) {
            return false;
        }

        ModDependency other = (ModDependency) obj;

        if (!this.name.equals(other.name)) {
            return false;
        }

        if (!this.version.equals(other.version)) {
            return false;
        }

        return true;
    }

    public String getDisplayName() {
        return this.name + " " + this.version;
    }

    public String getName() {
        return this.name;
    }

    public String getVersion() {
        return this.version;
    }

    public VersionRequirement getVersionRequirement() {
        if (this.versionRequirement == null) {
            this.versionRequirement = VersionRequirement.parse(this.version);
        }

        return this.versionRequirement;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.name == null ? 0 : this.name.hashCode());
        result = prime * result + (this.version == null ? 0 : this.version.hashCode());
        return result;
    }

    public boolean isSatisfiedBy(Version actualVersion) {
        return this.getVersionRequirement().isSatisfiedBy(actualVersion);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "(Dependency: " + this.name + ", " + this.version + ")";
    }
}
