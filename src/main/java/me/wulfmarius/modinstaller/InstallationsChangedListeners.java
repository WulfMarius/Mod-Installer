package me.wulfmarius.modinstaller;

public class InstallationsChangedListeners extends Listeners<InstallationsChangedListener> {

    public void changed() {
        this.fire(listener -> listener.changed());
    }
}
