package me.wulfmarius.modinstaller;

import java.util.*;
import java.util.stream.Collectors;

import me.wulfmarius.modinstaller.repository.*;

public class DependencyResolver {

    private final Repository repository;
    private final Installations installations;

    private final ModDefinitions installed = new ModDefinitions();
    private final Resolution resolution = new Resolution();

    public DependencyResolver(Repository repository, Installations installations) {
        super();

        this.repository = repository;
        this.installations = installations;
    }

    public Installations getInstallations() {
        return this.installations;
    }

    public Repository getRepository() {
        return this.repository;
    }

    public Resolution resolve(ModDefinition modDefinition) {
        this.initializeInstalled();
        this.resolveDependencies(modDefinition);
        this.consolidate();

        return this.resolution;
    }

    private void consolidate() {
        for (Iterator<ModDefinition> iterator = this.resolution.getInstall().iterator(); iterator.hasNext();) {
            ModDefinition eachInstall = iterator.next();

            if (this.resolution.getUninstall().contains(eachInstall)) {
                iterator.remove();
                this.resolution.getUninstall().remove(eachInstall);
                continue;
            }

            if (this.installations.contains(eachInstall)) {
                iterator.remove();
            }
        }

        this.resolution.getInstall().reverse();
    }

    private DependencyResolution findMatchingVersion(ModDependencies dependencies) {
        DependencyResolution result = new DependencyResolution();
        result.setRequested(dependencies);

        Map<ModDependency, ModDefinitions> matchingDependencies = new LinkedHashMap<ModDependency, ModDefinitions>();

        for (ModDependency eachDependency : dependencies) {
            matchingDependencies.put(eachDependency, this.repository.getMatching(eachDependency));
        }

        result.setAvailable(matchingDependencies.values().stream().flatMap(ModDefinitions::stream).collect(Collectors.toSet()));

        ModDefinitions candidates = matchingDependencies.values().stream().reduce(null, ModDefinitions::intersect);
        candidates.getMin(ModDefinition::latest).ifPresent(result::setBestMatch);

        return result;
    }

    private void initializeInstalled() {
        this.installations.stream()
                .map(installation -> this.repository.getModDefinition(installation.getName(), installation.getVersion()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(this.installed::addModDefinition);
    }

    private void install(ModDefinition bestMatch) {
        this.resolution.addInstall(bestMatch);
        this.installed.addModDefinition(bestMatch);
    }

    private void resolveDependencies(ModDefinition modDefinition) {
        ModDefinition next = modDefinition;

        while (next != null) {
            this.uninstall(next);
            this.install(next);

            DependencyResolution dependencyResolution = this.installed.getAllDependencies()
                    .values()
                    .stream()
                    .filter(dependencies -> !this.installed.satisfiesAll(dependencies))
                    .map(this::findMatchingVersion)
                    .findFirst()
                    .orElseGet(DependencyResolution::empty);

            if (!dependencyResolution.isAvailable()) {
                this.resolution.setMissingDependencies(dependencyResolution.getRequested());
            } else if (!dependencyResolution.isResolved()) {
                this.resolution.setUnresolvableDependencies(dependencyResolution.getRequested());
            }

            next = dependencyResolution.getBestMatch();
        }
    }

    private void uninstall(ModDefinition modDefinition) {
        ModDefinitions installedVersions = this.installed.getModDefinitions(modDefinition.getName());
        for (ModDefinition eachInstalledVersion : installedVersions) {
            this.installed.remove(eachInstalledVersion);
        }

        this.resolution.addUninstalls(this.installations.getInstallations(modDefinition.getName()));
    }
}
