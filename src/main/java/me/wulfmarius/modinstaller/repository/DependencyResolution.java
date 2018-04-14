package me.wulfmarius.modinstaller.repository;

import java.util.*;

import me.wulfmarius.modinstaller.*;

public class DependencyResolution {

    private List<ModDependency> dependencies = new ArrayList<>();
    private ModDefinition bestMatch;
    private Set<ModDefinition> available = new HashSet<>();

    public Set<ModDefinition> getAvailable() {
        return this.available;
    }

    public ModDefinition getBestMatch() {
        return this.bestMatch;
    }

    public List<ModDependency> getRequested() {
        return this.dependencies;
    }

    public boolean isResolved() {
        return this.bestMatch != null;
    }

    public void setAvailable(Set<ModDefinition> available) {
        this.available = available;
    }

    public void setBestMatch(ModDefinition bestMatch) {
        this.bestMatch = bestMatch;
    }

    public void setDependencies(List<ModDependency> dependencies) {
        this.dependencies = dependencies;
    }
}
