package me.wulfmarius.modinstaller.ui;

import static me.wulfmarius.modinstaller.ui.ControllerFactory.CONTROLLER_FACTORY;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.util.*;

import javafx.application.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.*;
import me.wulfmarius.modinstaller.*;

public class ModInstallerUI extends Application {

    private static Image ICON = new Image("/icon.png");

    private static HostServices hostServices;
    private static Stage mainStage;

    public static void setTitle(String title) {
        mainStage.setTitle(title);
    }

    protected static String formatReleaseDate(Date date) {
        return DateFormat.getDateInstance(DateFormat.SHORT).format(date);
    }

    protected static void openURL(String url) {
        try {
            hostServices.showDocument(url);
        } catch (Exception e) {
            showError("Could Not Open", "Could not open URL '" + url + "': " + e.getMessage());
        }
    }

    protected static void showError(String title, String message) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> showError(title, message));
            return;
        }

        Alert dialog = new Alert(AlertType.ERROR);
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.getDialogPane().getStylesheets().add(ModInstaller.class.getResource("/global.css").toExternalForm());
        dialog.setContentText(message);
        ((Stage) dialog.getDialogPane().getScene().getWindow()).getIcons().add(ICON);
        dialog.showAndWait();
    }

    protected static Optional<ButtonType> showYesNoChoice(String title, String message) {
        Alert dialog = new Alert(AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        ((Stage) dialog.getDialogPane().getScene().getWindow()).getIcons().add(ICON);
        return dialog.showAndWait();
    }

    protected static void showYesNoChoice(String title, String message, Runnable onYes) {
        if (ModInstallerUI.showYesNoChoice(title, message).filter(ButtonType.YES::equals).isPresent()) {
            onYes.run();
        }
    }

    protected static void startAutoCloseProgressDialog(String title, Node ownerNode, Runnable runnable) {
        progressDialog(title, ownerNode, runnable, null, true);
    }

    protected static void startAutoCloseProgressDialog(String title, Node ownerNode, Runnable runnable, Runnable onClosed) {
        progressDialog(title, ownerNode, runnable, onClosed, true);
    }

    protected static void startChangeLogViewer(Node ownerNode, ModDefinition modDefinition) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(ProgressDialogController.class.getResource("ChangeLogViewer.fxml"));
            fxmlLoader.setControllerFactory(CONTROLLER_FACTORY);
            fxmlLoader.load();
            ChangeLogViewerController controller = fxmlLoader.getController();
            controller.setModDefinition(modDefinition);

            Stage stage = new Stage();
            stage.setScene(new Scene(fxmlLoader.getRoot()));
            stage.setTitle("Change Log - " + modDefinition.getName());
            stage.getIcons().add(ICON);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(ownerNode.getScene().getWindow());
            stage.setResizable(false);
            stage.centerOnScreen();

            stage.show();
        } catch (IOException e) {
            showError("Could not show change log viewer", e.getMessage());
        }
    }

    protected static void startProgressDialog(String title, Node ownerNode, Runnable runnable) {
        progressDialog(title, ownerNode, runnable, null, false);
    }

    protected static void startProgressDialog(String title, Node ownerNode, Runnable runnable, Runnable onClosed) {
        progressDialog(title, ownerNode, runnable, onClosed, false);
    }

    private static void progressDialog(String title, Node ownerNode, Runnable runnable, Runnable onClosed,
            boolean autoCloseWithoutErrors) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> progressDialog(title, ownerNode, runnable, onClosed, autoCloseWithoutErrors));
            return;
        }

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(ProgressDialogController.class.getResource("ProgressDialog.fxml"));
            fxmlLoader.setControllerFactory(CONTROLLER_FACTORY);
            fxmlLoader.load();
            ProgressDialogController controller = fxmlLoader.getController();
            controller.setRunnable(runnable);
            controller.setAutoCloseWithoutErrors(autoCloseWithoutErrors);

            Stage stage = new Stage();
            stage.setScene(new Scene(fxmlLoader.getRoot()));
            stage.setTitle(title);
            stage.getIcons().add(ICON);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(ownerNode.getScene().getWindow());

            if (onClosed != null) {
                stage.setOnHidden(event -> onClosed.run());
            }

            stage.show();
        } catch (IOException e) {
            showError("Could Not Show Dialog", e.getMessage());
        }
    }

    @Override
    public void start(Stage primaryStage) throws IOException, URISyntaxException {
        if (System.getProperty("SKIP_DIRECTORY_CHECK") == null && !ControllerFactory.isInstallationDirectoryValid()) {
            showError("Invalid Installation Directory",
                    "Mod-Installer appears to be in the wrong directory.\n\n"
                            + "Make sure you put it into the directory \"TheLongDark\", which contains the \"tld\" executable.\n\n"
                            + "Working directory is " + ControllerFactory.getBaseDirectory());
            primaryStage.close();
            return;
        }

        hostServices = this.getHostServices();
        mainStage = primaryStage;

        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("InstallerMainPanel.fxml"));
        fxmlLoader.setControllerFactory(ControllerFactory.CONTROLLER_FACTORY);
        Parent mainPanel = fxmlLoader.load();

        Scene scene = new Scene(mainPanel);
        scene.getStylesheets().add("global.css");

        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.setTitle("TLD Mod-Installer " + ModInstaller.VERSION);
        primaryStage.getIcons().add(ICON);
        primaryStage.show();

        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(350);
    }
}
