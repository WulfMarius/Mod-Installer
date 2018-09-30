package me.wulfmarius.modinstaller;

import java.util.*;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ModDefinition {

    private String sourceDefinition;
    private String url;

    private String name;
    private String version;
    private String author;
    private String description;
    private String changes;
    private Date releaseDate;
    private Date lastUpdated;
    private Asset[] assets;
    private ModDependency[] dependencies;

    @JsonIgnore
    private transient Version parsedVersion;

    public static int compare(ModDefinition m1, ModDefinition m2) {
        return -Version.compare(m1.getParsedVersion(), m2.getParsedVersion());
    }

    public boolean dependsOn(String modName) {
        return this.getDependenciesStream().anyMatch(dependency -> dependency.getName().equals(modName));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ModDefinition)) {
            return false;
        }

        ModDefinition other = (ModDefinition) obj;
        if (!this.name.equals(other.name)) {
            return false;
        }

        if (this.getParsedVersion().compareTo(other.getParsedVersion()) != 0) {
            return false;
        }

        return true;
    }

    public Asset[] getAssets() {
        return this.assets;
    }

    public String getAuthor() {
        return this.author;
    }

    public String getChanges() {
        return this.changes;
    }

    public ModDependency[] getDependencies() {
        return this.dependencies;
    }

    public Stream<ModDependency> getDependenciesStream() {
        if (this.dependencies == null) {
            return Stream.empty();
        }

        return Arrays.stream(this.dependencies);
    }

    public String getDescription() {
        return this.description;
    }

    public String getDisplayName() {
        return this.name + " " + this.version;
    }

    public Date getLastUpdated() {
        return this.lastUpdated;
    }

    public String getName() {
        return this.name;
    }

    public Date getReleaseDate() {
        return this.releaseDate;
    }

    public String getSourceDefinition() {
        return this.sourceDefinition;
    }

    public String getUrl() {
        return this.url;
    }

    public String getVersion() {
        return this.version;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.name == null ? 0 : this.name.hashCode());
        result = prime * result + (this.version == null ? 0 : this.version.hashCode());
        return result;
    }

    public boolean satisfies(ModDependency dependency) {
        if (!this.name.equals(dependency.getName())) {
            return false;
        }

        return dependency.isSatisfiedBy(this.getParsedVersion());
    }

    public void setAssets(Asset[] assets) {
        this.assets = assets;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setChanges(String changes) {
        this.changes = changes;
    }

    public void setDependencies(ModDependency[] dependencies) {
        this.dependencies = dependencies;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public void setSourceDefinition(String sourceDefinition) {
        this.sourceDefinition = sourceDefinition;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "(Definition " + this.name + ", " + this.version + ")";
    }

    private Version getParsedVersion() {
        if (this.parsedVersion == null) {
            this.parsedVersion = Version.parse(this.version);
        }

        return this.parsedVersion;
    }
}
