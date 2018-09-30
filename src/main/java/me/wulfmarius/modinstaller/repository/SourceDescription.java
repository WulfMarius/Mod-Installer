package me.wulfmarius.modinstaller.repository;

import java.util.*;

import me.wulfmarius.modinstaller.ModDefinition;

public class SourceDescription {

    private String name;
    private String author;
    private String url;
    private String description;
    private ModDefinition[] releases;
    private String[] definitions;
    private Map<String, String> parameters = new HashMap<>();

    public String getAuthor() {
        return this.author;
    }

    public String[] getDefinitions() {
        return this.definitions;
    }

    public String getDescription() {
        return this.description;
    }

    public String getName() {
        return this.name;
    }

    public Map<String, String> getParameters() {
        return this.parameters;
    }

    public ModDefinition[] getReleases() {
        return this.releases;
    }

    public String getUrl() {
        return this.url;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setDefinitions(String[] definitions) {
        this.definitions = definitions;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParameter(String name, String value) {
        this.parameters.put(name, value);
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public void setReleases(ModDefinition[] releases) {
        this.releases = releases;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
