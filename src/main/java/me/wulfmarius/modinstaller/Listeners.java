package me.wulfmarius.modinstaller;

import java.util.*;
import java.util.function.Consumer;

public class Listeners<T> {

    private final Set<T> listeners = new HashSet<>();

    public void addListener(T listener) {
        this.listeners.add(listener);
    }

    public void fire(Consumer<? super T> action) {
        this.listeners.forEach(action);
    }

    public void removeListener(T listener) {
        this.listeners.remove(listener);
    }
}
