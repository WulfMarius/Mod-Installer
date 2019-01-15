package me.wulfmarius.modinstaller.repository;

import java.util.*;
import java.util.stream.*;

import org.springframework.util.StringUtils;

import me.wulfmarius.modinstaller.*;

public class Source {

    public static final String VERSION = "3";

    private String definition;
    private String name;
    private String url;
    private String description;
    private String[] definitions;

    private final Map<String, String> parameters = new HashMap<>();

    private ModDefinitions modDefinitions = new ModDefinitions();

    public static Source from(String definition, SourceDescription sourceDescription) {
        Source result = new Source();

        result.setDefinition(definition);
        result.setName(sourceDescription.getName());
        result.setUrl(sourceDescription.getUrl());
        result.setDescription(sourceDescription.getDescription());
        result.setDefinitions(sourceDescription.getDefinitions());
        result.parameters.putAll(sourceDescription.getParameters());
        result.createModDefinitions(sourceDescription.getReleases());

        return result;
    }

    public String getDefinition() {
        return this.definition;
    }

    public String[] getDefinitions() {
        return this.definitions;
    }

    public String getDescription() {
        return this.description;
    }

    public Optional<Date> getLastUpdated() {
        return this.getModDefinitionStream().map(ModDefinition::getLastUpdated).max(Date::compareTo);
    }

    public Stream<ModDefinition> getLatestVersions() {
        Map<String, Optional<ModDefinition>> collect = this.modDefinitions.stream()
                .collect(Collectors.groupingBy(ModDefinition::getName, Collectors.minBy(ModDefinition::latest)));
        return collect.values().stream().map(Optional::get);
    }

    public ModDefinitions getMatchingDefinitions(ModDependency dependency) {
        return this.modDefinitions.getMatchingDefinitions(dependency);
    }

    public Optional<ModDefinition> getModDefinition(String modDefinitionName, String modDefinitionVersion) {
        return this.modDefinitions.getModDefinition(modDefinitionName, modDefinitionVersion);
    }

    public ModDefinitions getModDefinitions() {
        return this.modDefinitions;
    }

    public Stream<ModDefinition> getModDefinitionStream() {
        return this.modDefinitions.stream();
    }

    public String getName() {
        return this.name;
    }

    public String getParameter(String parameterName) {
        if (this.parameters == null) {
            return null;
        }

        return this.parameters.get(parameterName);
    }

    public Map<String, String> getParameters() {
        return this.parameters;
    }

    public String getUrl() {
        return this.url;
    }

    public boolean hasParameterValue(String parameterName, String value) {
        if (value == null) {
            return this.getParameter(parameterName) == null;
        }

        return value.equals(this.getParameter(parameterName));
    }

    public void removeParameter(String parameterName) {
        this.parameters.remove(parameterName);
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public void setDefinitions(String[] definitions) {
        this.definitions = definitions;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setModDefinitions(ModDefinitions modDefinitions) {
        this.modDefinitions = modDefinitions;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void update(Source refreshedSource) {
        this.name = refreshedSource.name;
        this.description = refreshedSource.description;
        this.modDefinitions = refreshedSource.modDefinitions;

        this.parameters.clear();
        this.parameters.putAll(refreshedSource.parameters);
    }

    private void createModDefinitions(ModDefinition[] releases) {
        if (releases == null) {
            return;
        }

        for (ModDefinition eachModDefinition : releases) {
            if (StringUtils.isEmpty(eachModDefinition.getName())) {
                eachModDefinition.setName(this.name);
            }

            if (StringUtils.isEmpty(eachModDefinition.getUrl())) {
                eachModDefinition.setUrl(this.url);
            }
            if (StringUtils.isEmpty(eachModDefinition.getUrl())) {
                eachModDefinition.setUrl(this.definition);
            }

            if (StringUtils.isEmpty(eachModDefinition.getDescription())) {
                eachModDefinition.setDescription(this.description);
            }

            eachModDefinition.setLastUpdated(new Date());

            this.modDefinitions.addModDefinition(eachModDefinition);
        }
    }
}
