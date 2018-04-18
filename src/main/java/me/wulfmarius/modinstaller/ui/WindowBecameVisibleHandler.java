package me.wulfmarius.modinstaller.ui;

import javafx.beans.value.ObservableValue;
import javafx.scene.*;
import javafx.stage.*;

public class WindowBecameVisibleHandler {

    private final Runnable runnable;

    private WindowBecameVisibleHandler(Runnable runnable) {
        super();
        this.runnable = runnable;
    }

    public static void install(Node node, Runnable runnable) {
        WindowBecameVisibleHandler windowBecameVisibleHandler = new WindowBecameVisibleHandler(runnable);
        node.sceneProperty().addListener(windowBecameVisibleHandler::onSceneChanged);
    }

    @SuppressWarnings("unused")
    private void onSceneChanged(ObservableValue<? extends Scene> observable, Scene oldScene, Scene newScene) {
        if (newScene == null) {
            return;
        }

        newScene.windowProperty().addListener(this::onWindowChanged);
    }

    @SuppressWarnings("unused")
    private void onWindowChanged(ObservableValue<? extends Window> observable, Window oldWindow, Window newWindow) {
        if (newWindow == null) {
            return;
        }

        newWindow.addEventHandler(WindowEvent.WINDOW_SHOWN, event -> new Thread(this.runnable).start());
    }
}
