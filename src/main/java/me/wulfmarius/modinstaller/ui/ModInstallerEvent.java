package me.wulfmarius.modinstaller.ui;

import javafx.event.*;
import me.wulfmarius.modinstaller.ModDefinition;

public class ModInstallerEvent extends Event {

    private static final long serialVersionUID = 1L;

    public static EventType<ModInstallerEvent> MOD_INSTALLER = new EventType<>("MOD_INSTALLER");
    public static EventType<ModInstallerEvent> MOD_SELECTED = new EventType<>(MOD_INSTALLER, "MOD_SELECTED");

    private ModDefinition modDefinition;

    private ModInstallerEvent(EventType<? extends Event> eventType) {
        super(eventType);
    }

    public static ModInstallerEvent modSelected(ModDefinition modDefinition) {
        return new ModInstallerEvent(MOD_SELECTED).setModDefinition(modDefinition);
    }

    public ModDefinition getModDefinition() {
        return this.modDefinition;
    }

    private ModInstallerEvent setModDefinition(ModDefinition modDefinition) {
        this.modDefinition = modDefinition;
        return this;
    }
}
