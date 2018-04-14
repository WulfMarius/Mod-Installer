package me.wulfmarius.modinstaller;

import java.util.*;
import java.util.stream.*;

import com.fasterxml.jackson.annotation.*;

import me.wulfmarius.modinstaller.repository.DependencyResolution;

public class ModDefinitions implements Iterable<ModDefinition> {

    @JsonValue
    private List<ModDefinition> modDefinitions = new ArrayList<>();

    @JsonCreator
    public static ModDefinitions create(ModDefinition[] modDefinitions) {
        ModDefinitions result = new ModDefinitions();

        if (modDefinitions != null) {
            for (ModDefinition eachModDefinition : modDefinitions) {
                result.addModDefinition(eachModDefinition);
            }
        }

        return result;
    }

    private static <T> List<T> createList(T value) {
        ArrayList<T> result = new ArrayList<>();
        result.add(value);
        return result;
    }

    private static <T> List<T> mergeLists(List<T> list1, List<T> list2) {
        List<T> result = new ArrayList<>();
        result.addAll(list1);
        result.addAll(list2);
        return result;
    }

    private static <T> List<T> retain(List<T> list1, List<T> list2) {
        if (list1 == null) {
            return new ArrayList<>(list2);
        }

        if (list2 == null) {
            return new ArrayList<>(list1);
        }

        list1.retainAll(list2);
        return list1;
    }

    public void addModDefinition(ModDefinition definition) {
        if (!this.modDefinitions.contains(definition)) {
            this.modDefinitions.add(definition);
        }
    }

    public boolean contains(ModDefinition definition) {
        return this.modDefinitions.contains(definition);
    }

    public ModDefinitions getCopy() {
        ModDefinitions result = new ModDefinitions();

        result.modDefinitions.addAll(this.modDefinitions);

        return result;
    }

    public List<ModDefinition> getDefinition(String name) {
        return this.modDefinitions.stream().filter(modDefinition -> modDefinition.getName().equals(name)).collect(Collectors.toList());
    }

    public Optional<ModDefinition> getDefinition(String name, String version) {
        return this.modDefinitions.stream()
                .filter(definition -> definition.getName().equals(name) && definition.getVersion().equals(version)).findFirst();
    }

    public List<ModDefinition> getMatchingDefinitions(ModDependency dependency) {
        return this.modDefinitions.stream().filter(definition -> definition.satisfies(dependency)).collect(Collectors.toList());
    }

    public Map<String, List<ModDependency>> getMissingDependencies() {
        return this.modDefinitions.stream().flatMap(ModDefinition::getDependenciesStream)
                .filter(dependency -> !this.satisfies(dependency))
                .collect(Collectors.toMap(ModDependency::getName, ModDefinitions::createList, ModDefinitions::mergeLists));
    }

    public List<ModDefinition> getModDefinitions() {
        return this.modDefinitions;
    }

    @Override
    public Iterator<ModDefinition> iterator() {
        return this.modDefinitions.iterator();
    }

    public void remove(ModDefinition modDefinition) {
        this.modDefinitions.remove(modDefinition);
    }

    public void remove(String name) {
        if (name == null) {
            return;
        }

        this.modDefinitions.removeIf(modDefinition -> name.equals(modDefinition.getName()));
    }

    public DependencyResolution resolve(List<ModDependency> dependencies) {
        DependencyResolution result = new DependencyResolution();
        result.setDependencies(dependencies);

        List<List<ModDefinition>> matchingDependencies = dependencies.stream().map(this::getMatchingDefinitions)
                .collect(Collectors.toList());

        result.setAvailable(matchingDependencies.stream().flatMap(List::stream).collect(Collectors.toSet()));

        List<ModDefinition> candidates = matchingDependencies.stream().reduce(null, ModDefinitions::retain);
        candidates.sort(ModDefinition::compare);
        if (!candidates.isEmpty()) {
            result.setBestMatch(candidates.get(0));
        }

        return result;
    }

    public boolean satisfies(ModDependency modDependency) {
        return this.modDefinitions.stream().anyMatch(modDefinition -> modDefinition.satisfies(modDependency));
    }

    public void setModDefinitions(List<ModDefinition> modDefinitions) {
        this.modDefinitions = modDefinitions;
    }

    public Stream<ModDefinition> stream() {
        return this.modDefinitions.stream();
    }
}
