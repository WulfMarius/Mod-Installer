package me.wulfmarius.modinstaller.repository;

import java.util.*;

import me.wulfmarius.modinstaller.ModDefinition;

public class Installation {

    private String sourceDefinition;
    private String name;
    private String version;
    private List<String> assets = new ArrayList<>();

    public void addAsset(String asset) {
        if (asset == null) {
            return;
        }

        this.assets.add(asset);
    }

    public List<String> getAssets() {
        return this.assets;
    }

    public String getDisplayName() {
        return this.name + " " + this.version;
    }

    public String getName() {
        return this.name;
    }

    public String getSourceDefinition() {
        return this.sourceDefinition;
    }

    public String getVersion() {
        return this.version;
    }

    public boolean isAssetReferenced(String asset) {
        return this.assets.contains(asset);
    }

    public boolean matches(ModDefinition modDefinition) {
        return this.name.equals(modDefinition.getName()) && this.version.equals(modDefinition.getVersion());
    }

    public void setAssets(List<String> assets) {
        this.assets = assets;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSourceDefinition(String sourceDefinition) {
        this.sourceDefinition = sourceDefinition;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
