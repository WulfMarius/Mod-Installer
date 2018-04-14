package me.wulfmarius.modinstaller;

public class SourcesChangedListeners extends Listeners<SourcesChangedListener> {

    public void changed() {
        this.fire(listener -> listener.changed());
    }
}
