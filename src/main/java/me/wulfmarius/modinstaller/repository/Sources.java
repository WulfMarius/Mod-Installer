package me.wulfmarius.modinstaller.repository;

import java.util.*;
import java.util.stream.Stream;

import me.wulfmarius.modinstaller.ModDefinition;

public class Sources implements Iterable<Source> {

    private List<Source> sources = new ArrayList<>();

    public void addSource(Source source) {
        this.sources.add(source);
    }

    public boolean contains(String definition) {
        return this.sources.stream().anyMatch(source -> definition.equals(source.getDefinition()));
    }

    public List<Source> getSources() {
        return this.sources;
    }

    public boolean isEmpty() {
        return this.sources == null || this.sources.isEmpty();
    }

    @Override
    public Iterator<Source> iterator() {
        return this.sources.iterator();
    }

    public void removeSource(Source source) {
        this.sources.remove(source);
    }

    public Optional<ModDefinition> resolve(String name, String version) {
        return this.sources.stream().map(source -> source.getModDefinition(name, version)).filter(Optional::isPresent)
                .map(Optional::get).findFirst();
    }

    public void setSources(List<Source> sources) {
        this.sources = sources;
    }

    public int size() {
        if (this.sources == null) {
            return 0;
        }

        return this.sources.size();
    }

    public Stream<Source> stream() {
        if (this.sources == null) {
            return Stream.empty();
        }

        return this.sources.stream();
    }
}
