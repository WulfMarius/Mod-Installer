package me.wulfmarius.modinstaller.repository;

import java.util.*;
import java.util.stream.*;

import me.wulfmarius.modinstaller.ModDefinition;

public class Installations implements Iterable<Installation> {

    private List<Installation> installations = new ArrayList<>();

    public void addInstallation(Installation installation) {
        this.installations.add(installation);
    }

    public boolean contains(Installation installation) {
        return this.installations.contains(installation);
    }

    public boolean contains(ModDefinition modDefinition) {
        return this.installations.stream().anyMatch(installation -> installation.matches(modDefinition));
    }

    public List<Installation> getInstallations() {
        return this.installations;
    }

    public List<Installation> getInstallations(String name) {
        return this.installations.stream().filter(installation -> installation.getName().equals(name)).collect(Collectors.toList());
    }

    public List<Installation> getInstallationsWithAsset(String asset) {
        return this.installations.stream().filter(installation -> installation.isAssetReferenced(asset)).collect(Collectors.toList());
    }

    @Override
    public Iterator<Installation> iterator() {
        return this.installations.iterator();
    }

    public void remove(Installation installation) {
        this.installations.remove(installation);
    }

    public void setInstallations(List<Installation> installations) {
        this.installations = installations;
    }

    public Stream<Installation> stream() {
        return this.installations.stream();
    }
}
