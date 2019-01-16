package me.wulfmarius.modinstaller.repository;

import java.util.*;

import me.wulfmarius.modinstaller.*;

public class DependencyResolution {

    private ModDependencies requested;
    private ModDefinition bestMatch;
    private Set<ModDefinition> available = new HashSet<>();

    public static DependencyResolution empty() {
        return new DependencyResolution();
    }

    public Set<ModDefinition> getAvailable() {
        return this.available;
    }

    public ModDefinition getBestMatch() {
        return this.bestMatch;
    }

    public ModDependencies getRequested() {
        return this.requested;
    }

    public boolean isAvailable() {
        return this.available != null && !this.available.isEmpty();
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

    public void setRequested(ModDependencies requested) {
        this.requested = requested;
    }
}
