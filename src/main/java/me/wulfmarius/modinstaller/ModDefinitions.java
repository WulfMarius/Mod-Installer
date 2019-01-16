package me.wulfmarius.modinstaller;

import java.util.*;
import java.util.stream.*;

import com.fasterxml.jackson.annotation.*;

public class ModDefinitions implements Iterable<ModDefinition> {

    @JsonValue
    private List<ModDefinition> modDefinitions = new ArrayList<>();

    @JsonCreator
    public static ModDefinitions create(ModDefinition... modDefinition) {
        ModDefinitions result = new ModDefinitions();

        if (modDefinition != null) {
            for (ModDefinition eachModDefinition : modDefinition) {
                result.addModDefinition(eachModDefinition);
            }
        }

        return result;
    }

    public static ModDefinitions intersect(ModDefinitions definitions1, ModDefinitions definitions2) {
        if (definitions1 == null) {
            return definitions2;
        }

        if (definitions2 == null) {
            return definitions1;
        }

        ModDefinitions result = new ModDefinitions();

        for (ModDefinition eachDefinition : definitions1) {
            if (definitions2.contains(eachDefinition)) {
                result.addModDefinition(eachDefinition);
            }
        }

        return result;
    }

    public static ModDefinitions merge(ModDefinitions definitions1, ModDefinitions definitions2) {
        ModDefinitions result = new ModDefinitions();

        result.addModDefinitions(definitions1);
        result.addModDefinitions(definitions2);

        return result;
    }

    public static Collector<ModDefinition, ?, ModDefinitions> toModDefinitions() {
        return Collectors.reducing(new ModDefinitions(), ModDefinitions::create, ModDefinitions::merge);
    }

    public void addModDefinition(ModDefinition definition) {
        if (!this.modDefinitions.contains(definition)) {
            this.modDefinitions.add(definition);
        }
    }

    public void addModDefinitions(Iterable<ModDefinition> definitions) {
        definitions.forEach(this::addModDefinition);
    }

    public boolean contains(ModDefinition definition) {
        return this.modDefinitions.contains(definition);
    }

    public Map<String, ModDependencies> getAllDependencies() {
        return this.modDefinitions.stream().flatMap(ModDefinition::getDependenciesStream).collect(
                Collectors.toMap(ModDependency::getName, ModDependencies::create, ModDependencies::merge));
    }

    public ModDefinitions getMatchingDefinitions(ModDependency dependency) {
        return this.modDefinitions.stream().filter(definition -> definition.satisfies(dependency)).collect(toModDefinitions());
    }

    public Optional<ModDefinition> getMin(Comparator<? super ModDefinition> comparator) {
        return this.modDefinitions.stream().min(comparator);
    }

    public Optional<ModDefinition> getModDefinition(String name, String version) {
        return this.modDefinitions.stream()
                .filter(definition -> definition.getName().equals(name) && definition.getVersion().equals(version))
                .findFirst();
    }

    public ModDefinitions getModDefinitions(String name) {
        return this.modDefinitions.stream().filter(modDefinition -> modDefinition.getName().equals(name)).collect(toModDefinitions());
    }

    public int getSize() {
        if (this.modDefinitions.isEmpty()) {
            return 0;
        }

        return this.modDefinitions.size();
    }

    public boolean isEmpty() {
        return this.modDefinitions == null || this.modDefinitions.isEmpty();
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

    public void reverse() {
        Collections.reverse(this.modDefinitions);
    }

    public boolean satisfies(ModDependency modDependency) {
        return this.modDefinitions.stream().anyMatch(modDefinition -> modDefinition.satisfies(modDependency));
    }

    public boolean satisfiesAll(ModDependencies modDependencies) {
        return modDependencies.stream().allMatch(this::satisfies);
    }

    public Stream<ModDefinition> stream() {
        return this.modDefinitions.stream();
    }
}
