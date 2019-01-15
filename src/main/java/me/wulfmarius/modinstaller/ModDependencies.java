package me.wulfmarius.modinstaller;

import java.util.*;
import java.util.stream.Stream;

public class ModDependencies implements Iterable<ModDependency> {

    private Set<ModDependency> dependencies = new LinkedHashSet<>();

    public static ModDependencies create(ModDependency dependency) {
        ModDependencies result = new ModDependencies();
        result.dependencies.add(dependency);
        return result;
    }

    public static ModDependencies merge(ModDependencies dependencies1, ModDependencies dependencies2) {
        ModDependencies result = new ModDependencies();
        result.addAll(dependencies1);
        result.addAll(dependencies2);
        return result;
    }

    public void add(ModDependency modDependency) {
        this.dependencies.add(modDependency);
    }

    public void addAll(Iterable<ModDependency> additionalDependencies) {
        additionalDependencies.forEach(this::add);
    }

    public boolean contains(ModDependency modDependency) {
        return this.dependencies.contains(modDependency);
    }

    public int getSize() {
        if (this.isEmpty()) {
            return 0;
        }

        return this.dependencies.size();
    }

    public boolean isEmpty() {
        return this.dependencies == null || this.dependencies.isEmpty();
    }

    @Override
    public Iterator<ModDependency> iterator() {
        if (this.dependencies == null) {
            return Collections.emptyIterator();
        }

        return this.dependencies.iterator();
    }

    public Stream<ModDependency> stream() {
        if (this.dependencies == null) {
            return Stream.empty();
        }

        return this.dependencies.stream();
    }

    @Override
    public String toString() {
        return this.dependencies.toString();
    }
}
