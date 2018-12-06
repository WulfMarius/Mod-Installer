package me.wulfmarius.modinstaller.ui;

import static me.wulfmarius.modinstaller.ui.ModInstallerUI.formatReleaseDate;

import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.Callable;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import me.wulfmarius.modinstaller.*;

public class ChangeLogViewerController {

    @FXML
    private Pane pane;

    private ModInstaller modInstaller;

    public ChangeLogViewerController(ModInstaller modInstaller) {
        super();
        this.modInstaller = modInstaller;
    }

    public void setModDefinition(ModDefinition modDefinition) {
        List<ModDefinition> modDefinitions = this.modInstaller.getModDefinitions(modDefinition.getName());

        for (ModDefinition eachModDefinition : modDefinitions) {
            TextArea textArea = new TextArea(eachModDefinition.getChanges());
            textArea.setEditable(false);
            textArea.setWrapText(true);

            String title = MessageFormat.format("{0} - {1}", eachModDefinition.getVersion(),
                    formatReleaseDate(eachModDefinition.getReleaseDate()));
            TitledPane titledPane = new TitledPane(title, textArea);
            titledPane.setExpanded(this.pane.getChildren().isEmpty());
            this.pane.getChildren().add(titledPane);

            Platform.runLater(() -> this.adjustHeight(textArea));
        }
    }

    private void adjustHeight(TextArea textArea) {
        Node text = textArea.lookup(".text");
        if (text == null) {
            return;
        }

        textArea.prefHeightProperty().bind(Bindings.createDoubleBinding(new Callable<Double>() {

            @Override
            public Double call() throws Exception {
                return text.getBoundsInLocal().getHeight() + 10;
            }
        }, text.boundsInLocalProperty()));
    }

    @FXML
    private void onClose() {
        this.pane.getScene().getWindow().hide();
    }
}
