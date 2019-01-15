package me.wulfmarius.modinstaller.repository;

import java.util.*;
import java.util.stream.*;

import me.wulfmarius.modinstaller.ModDefinition;

public class Installations implements Iterable<Installation> {

    private List<Installation> installations = new ArrayList<>();

    public static Installations create(Installation... installation) {
        Installations result = new Installations();

        if (installation != null) {
            for (Installation eachInstallation : installation) {
                result.addInstallation(eachInstallation);
            }
        }

        return result;
    }

    public static Installations merge(Installations definitions1, Installations definitions2) {
        Installations result = new Installations();

        result.addInstallations(definitions1);
        result.addInstallations(definitions2);

        return result;
    }

    private static Collector<Installation, ?, Installations> toInstallations() {
        return Collectors.reducing(new Installations(), Installations::create, Installations::merge);
    }

    public void addInstallation(Installation installation) {
        this.installations.add(installation);
    }

    public void addInstallations(Iterable<Installation> otherInstallations) {
        for (Installation eachInstallation : otherInstallations) {
            this.addInstallation(eachInstallation);
        }
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

    public Installations getInstallations(String name) {
        return this.installations.stream().filter(installation -> installation.getName().equals(name)).collect(toInstallations());
    }

    public Installations getInstallationsWithAsset(String asset) {
        return this.installations.stream().filter(installation -> installation.isAssetReferenced(asset)).collect(toInstallations());
    }

    public int getSize() {
        if (this.installations == null) {
            return 0;
        }

        return this.installations.size();
    }

    public boolean isEmpty() {
        return this.installations == null || this.installations.isEmpty();
    }

    @Override
    public Iterator<Installation> iterator() {
        return this.installations.iterator();
    }

    public void remove(Installation installation) {
        this.installations.remove(installation);
    }

    public void remove(ModDefinition modDefinition) {
        this.installations.removeIf(installation -> installation.matches(modDefinition));
    }

    public void setInstallations(List<Installation> installations) {
        this.installations = installations;
    }

    public Stream<Installation> stream() {
        return this.installations.stream();
    }
}
