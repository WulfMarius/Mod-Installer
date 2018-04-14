package me.wulfmarius.modinstaller.ui;

import static me.wulfmarius.modinstaller.ui.ControllerFactory.CONTROLLER_FACTORY;
import static me.wulfmarius.modinstaller.ui.ModInstallerUI.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.css.PseudoClass;
import javafx.fxml.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import me.wulfmarius.modinstaller.*;
import me.wulfmarius.modinstaller.repository.Source;

public class InstallerMainPanelController {

    protected static final PseudoClass PSEUDO_CLASS_UPDATE_AVAILABLE = PseudoClass.getPseudoClass("update-available");

    protected static final PseudoClass PSEUDO_CLASS_NOT_INSTALLED = PseudoClass.getPseudoClass("not-installed");
    protected static final PseudoClass PSEUDO_CLASS_REQUIRED = PseudoClass.getPseudoClass("required");
    private final ModInstaller modInstaller;

    @FXML
    private Pane detailsPane;

    @FXML
    private TableView<ModDefinition> tableView;

    @FXML
    private TableColumn<ModDefinition, String> columnName;
    @FXML
    private TableColumn<ModDefinition, String> columnInstalledVersion;
    @FXML
    private TableColumn<ModDefinition, String> columnAvailableVersion;

    public InstallerMainPanelController(ModInstaller modInstaller) {
        super();
        this.modInstaller = modInstaller;
    }

    protected boolean isNotInstalled(ModDefinition modDefinition) {
        return !this.modInstaller.isAnyVersionInstalled(modDefinition);
    }

    protected boolean isRequired(ModDefinition modDefinition) {
        return this.modInstaller.isRequiredByInstallation(modDefinition);
    }

    protected boolean isUpdateAvailable(ModDefinition modDefinition) {
        return this.modInstaller.isOtherVersionInstalled(modDefinition);
    }

    @FXML
    private void addSource() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Source");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter Source");

        Optional<String> optional = dialog.showAndWait();
        optional.ifPresent(this::addSource);
    }

    private void addSource(String sourceDefinition) {
        startProgressDialog("Adding Source " + sourceDefinition, this.detailsPane);
        executeAsyncDelayed(() -> {
            this.modInstaller.registerSource(sourceDefinition);
        });
    }

    private void askToInstallSource() {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(this::askToInstallSource);
            return;
        }

        Alert alert = new Alert(AlertType.CONFIRMATION,
                "It looks like you don't have any sources yet. Would you like to import the default source now?", ButtonType.YES,
                ButtonType.NO);
        alert.setTitle("No Sources Found");
        alert.setHeaderText(null);

        if (alert.showAndWait().filter(ButtonType.YES::equals).isPresent()) {
            this.addSource(
                    "https://raw.githubusercontent.com/WulfMarius/Mod-Installer/master/descriptions/default-mod-installer-description.json");
        }
    }

    private TableCell<ModDefinition, String> createTableCell(@SuppressWarnings("unused") TableColumn<ModDefinition, String> column) {
        return new ModStatusTableCell();
    }

    private TableRow<ModDefinition> createTableRow(@SuppressWarnings("unused") TableView<ModDefinition> view) {
        return new ModStatusTableRow();
    }

    private ObservableValue<String> getInstalledVersion(CellDataFeatures<ModDefinition, String> features) {
        return new ReadOnlyStringWrapper(this.modInstaller.getInstalledVersion(features.getValue().getName()));
    }

    private List<ModDefinition> getModDefinitions() {
        return this.modInstaller.getSources().stream().flatMap(Source::getLatestVersions).collect(Collectors.toList());
    }

    @FXML
    private void initialize() throws IOException {
        this.modInstaller.addInstallationsChangedListener(this::installationsChanged);
        this.modInstaller.addSourcesChangedListener(this::sourcesChanged);

        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("ModDetailsPanel.fxml"));
        fxmlLoader.setControllerFactory(CONTROLLER_FACTORY);
        Node detailsPanel = fxmlLoader.load();
        this.detailsPane.getChildren().setAll(detailsPanel);

        this.columnName.setCellValueFactory(new PropertyValueFactory<>("name"));
        this.columnName.setCellFactory(this::createTableCell);
        this.columnAvailableVersion.setCellValueFactory(new PropertyValueFactory<>("version"));
        this.columnInstalledVersion.setCellValueFactory(this::getInstalledVersion);

        this.tableView.setRowFactory(this::createTableRow);
        this.tableView.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> detailsPanel.fireEvent(ModInstallerEvent.modSelected(newValue)));

        this.tableView.getSortOrder().add(this.columnName);
        this.sourcesChanged();

        if (this.modInstaller.getSources().isEmpty()) {
            ModInstallerUI.executeAsyncDelayed(this::askToInstallSource);
        }
    }

    private void installationsChanged() {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(this::installationsChanged);
            return;
        }

        this.tableView.refresh();
        this.tableView.sort();
    }

    @FXML
    private void refreshSources() {
        ModInstallerUI.startProgressDialog("Refreshing Sources", this.detailsPane);
        executeAsyncDelayed(() -> this.modInstaller.refreshSources());
    }

    private void sourcesChanged() {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(this::sourcesChanged);
            return;
        }

        this.tableView.getItems().setAll(this.getModDefinitions());
        this.tableView.sort();
    }

    protected class ModStatusTableCell extends TableCell<ModDefinition, String> {

        @Override
        protected void updateItem(String item, boolean empty) {
            if (item == this.getItem()) {
                return;
            }

            super.updateItem(item, empty);
            this.setText(item);
            this.setGraphic(null);

            ModDefinition modDefinition = (ModDefinition) this.getTableRow().getItem();
            if (modDefinition != null) {
                this.pseudoClassStateChanged(PSEUDO_CLASS_UPDATE_AVAILABLE,
                        InstallerMainPanelController.this.isUpdateAvailable(modDefinition));
                this.pseudoClassStateChanged(PSEUDO_CLASS_REQUIRED, InstallerMainPanelController.this.isRequired(modDefinition));
            }
        }
    }

    protected class ModStatusTableRow extends TableRow<ModDefinition> {

        @Override
        protected void updateItem(ModDefinition item, boolean empty) {
            super.updateItem(item, empty);

            if (!empty) {
                this.pseudoClassStateChanged(PSEUDO_CLASS_NOT_INSTALLED, InstallerMainPanelController.this.isNotInstalled(item));
            }
        }
    }
}
