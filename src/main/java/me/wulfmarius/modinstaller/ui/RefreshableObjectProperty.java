package me.wulfmarius.modinstaller.ui;

import javafx.beans.property.SimpleObjectProperty;

public class RefreshableObjectProperty<T> extends SimpleObjectProperty<T> {

    protected RefreshableObjectProperty(T initialValue) {
        super(initialValue);
    }

    @Override
    public void fireValueChangedEvent() {
        super.fireValueChangedEvent();
    }
}