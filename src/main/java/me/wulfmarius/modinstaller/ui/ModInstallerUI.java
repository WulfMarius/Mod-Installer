package me.wulfmarius.modinstaller.ui;

import static me.wulfmarius.modinstaller.ui.ControllerFactory.CONTROLLER_FACTORY;

import java.io.IOException;
import java.nio.file.*;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.*;

public class ModInstallerUI extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    protected static void executeAsyncDelayed(Runnable runnable) {
        new Thread(() -> {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            runnable.run();
        }).start();
    }

    protected static void startProgressDialog(String title, Node ownerNode) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(ProgressDialogController.class.getResource("ProgressDialog.fxml"));
            fxmlLoader.setControllerFactory(CONTROLLER_FACTORY);
            fxmlLoader.load();

            Stage stage = new Stage();
            stage.getIcons().add(new Image(ModInstallerUI.class.getResourceAsStream("/icon.png")));
            stage.setScene(new Scene(fxmlLoader.getRoot()));
            stage.setTitle(title);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(ownerNode.getScene().getWindow());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean verifyInstallationDirectory() {
        // return true;
        return Files.exists(Paths.get("./TLD.exe")) || Files.exists(Paths.get("./tld.app")) || Files.exists(Paths.get("./tld.x86"))
                || Files.exists(Paths.get("./tld.x86_64"));
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        if (!verifyInstallationDirectory()) {
            Alert dialog = new Alert(AlertType.WARNING);
            dialog.setTitle("Invalid Installation Directory");
            dialog.setHeaderText(null);
            dialog.setContentText(
                    "Mod-Installer appears to be in the wrong directory.\nMake sure you put it into the directory \"TheLongDark\", which contains the \"tld\" executable");

            dialog.showAndWait();
            primaryStage.close();
            return;
        }

        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("InstallerMainPanel.fxml"));
        fxmlLoader.setControllerFactory(ControllerFactory.CONTROLLER_FACTORY);
        Parent mainPanel = fxmlLoader.load();

        Scene scene = new Scene(mainPanel);
        scene.getStylesheets().add("global.css");

        primaryStage.getIcons().add(new Image(this.getClass().getResourceAsStream("/icon.png")));
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.setTitle("TLD Mod Installer 0.1.0");
        primaryStage.show();
    }
}
