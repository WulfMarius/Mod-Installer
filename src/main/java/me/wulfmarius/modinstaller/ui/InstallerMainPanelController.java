package me.wulfmarius.modinstaller.ui;

import static me.wulfmarius.modinstaller.ui.ControllerFactory.CONTROLLER_FACTORY;
import static me.wulfmarius.modinstaller.ui.ModInstallerUI.*;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.css.PseudoClass;
import javafx.fxml.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.*;
import javafx.scene.layout.Pane;
import javafx.util.Callback;
import me.wulfmarius.modinstaller.*;
import me.wulfmarius.modinstaller.compatibility.CompatibilityChecker.Compatibility;
import me.wulfmarius.modinstaller.update.UpdateState;
import me.wulfmarius.modinstaller.utils.OsUtils;

public class InstallerMainPanelController {

    protected static final PseudoClass PSEUDO_CLASS_UPDATE_AVAILABLE = PseudoClass.getPseudoClass("update-available");
    protected static final PseudoClass PSEUDO_CLASS_RECENT = PseudoClass.getPseudoClass("recent");
    protected static final PseudoClass PSEUDO_CLASS_NOT_INSTALLED = PseudoClass.getPseudoClass("not-installed");
    protected static final PseudoClass PSEUDO_CLASS_REQUIRED = PseudoClass.getPseudoClass("required");
    protected static final PseudoClass PSEUDO_CLASS_INCOMPATIBLE = PseudoClass.getPseudoClass("incompatible");

    private final ModInstaller modInstaller;

    @FXML
    private Node root;

    @FXML
    private Pane detailsPane;

    @FXML
    private TableView<ModDefinition> tableView;

    @FXML
    private ComboBox<ModDefinitionFilter> comboBoxFilter;

    @FXML
    private TableColumn<ModDefinition, String> columnName;
    @FXML
    private TableColumn<ModDefinition, String> columnAuthor;
    @FXML
    private TableColumn<ModDefinition, String> columnInstalledVersion;
    @FXML
    private TableColumn<ModDefinition, String> columnAvailableVersion;
    @FXML
    private TableColumn<ModDefinition, Date> columnReleaseDate;

    public InstallerMainPanelController(ModInstaller modInstaller) {
        super();
        this.modInstaller = modInstaller;
    }

    public static <R, T> Callback<TableColumn<R, T>, TableCell<R, T>> getFormattedCell(Function<T, String> formatter) {
        return column -> {
            return new TableCell<R, T>() {

                @Override
                protected void updateItem(T item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        this.setText(null);
                    } else {
                        this.setText(formatter.apply(item));
                    }
                }
            };
        };
    }

    public boolean isRecent(ModDefinition modDefinition) {
        if (modDefinition.getLastUpdated() == null) {
            return false;
        }

        return modDefinition.getLastUpdated().getTime() >= System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2)
                && this.isNotInstalled(modDefinition);
    }

    protected List<ModDefinition> getModDefinitions(ModDefinitionFilter filter) {
        return this.modInstaller.getLatestVersions().stream().filter(filter).collect(Collectors.toList());
    }

    protected boolean isIncompatible(ModDefinition modDefinition) {
        return this.modInstaller.getCompatibility(modDefinition) != Compatibility.OK;
    }

    protected boolean isInstalled(ModDefinition modDefinition) {
        return this.modInstaller.isAnyVersionInstalled(modDefinition);
    }

    protected boolean isNotInstalled(ModDefinition modDefinition) {
        return this.modInstaller.isNoVersionInstalled(modDefinition);
    }

    protected boolean isRequired(ModDefinition modDefinition) {
        return this.modInstaller.isRequiredByInstallation(modDefinition);
    }

    protected boolean isUpdateAvailable(ModDefinition modDefinition) {
        return this.modInstaller.isOlderVersionInstalled(modDefinition);
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
        startProgressDialog("Adding Source " + sourceDefinition,
                this.detailsPane,
                () -> this.modInstaller.registerSource(sourceDefinition));
    }

    private void askDeleteOtherVersions() {
        Path[] otherVersions = this.modInstaller.getOtherVersions();
        String message = Arrays.stream(otherVersions).map(Path::getFileName).map(Path::toString).collect(Collectors.joining("\n",
                "The following other versions of Mod-Installer are still present:\n",
                "\n\nDo you want to delete them now?"));

        ModInstallerUI.showYesNoChoice("Other Versions Present", message, () -> {
            for (Path eachOldVersion : otherVersions) {
                try {
                    Files.deleteIfExists(eachOldVersion);
                } catch (IOException e) {
                    showError("Could Not Delete", "Could not delete " + eachOldVersion.getFileName() + ": " + e.getMessage());
                }
            }
        });
    }

    private void askDownloadNewVersion() {
        UpdateState updateState = this.modInstaller.getUpdateState();

        String message;
        if (updateState.hasAsset()) {
            message = "Version " + updateState.getLatestVersion() + " is available for download.\n\nWould you like to download it now?";
        } else {
            message = "Version " + updateState.getLatestVersion()
                    + " is available for download.\n\nWould you like to go to the download page?";
        }

        ModInstallerUI.showYesNoChoice("Update Available", message, () -> {
            if (updateState.hasAsset()) {
                startProgressDialog("Download Update", this.root, this.modInstaller::prepareUpdate, this::startUpdate);
            } else {
                openURL(updateState.getReleaseUrl());
            }
        });
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

    @FXML
    private void initialize() throws IOException {
        WindowBecameVisibleHandler.install(this.root, this::initializeModInstaller);
        this.modInstaller.addInstallationsChangedListener(this::updateModDefinitions);
        this.modInstaller.addSourcesChangedListener(this::updateModDefinitions);

        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("ModDetailsPanel.fxml"));
        fxmlLoader.setControllerFactory(CONTROLLER_FACTORY);
        Node detailsPanel = fxmlLoader.load();
        this.detailsPane.getChildren().setAll(detailsPanel);

        this.columnName.setCellValueFactory(new PropertyValueFactory<>("name"));
        this.columnName.setCellFactory(this::createTableCell);
        this.columnAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
        this.columnAvailableVersion.setCellValueFactory(new PropertyValueFactory<>("version"));
        this.columnInstalledVersion.setCellValueFactory(this::getInstalledVersion);
        this.columnReleaseDate.setCellValueFactory(new PropertyValueFactory<>("releaseDate"));
        this.columnReleaseDate.setCellFactory(getFormattedCell(ModInstallerUI::formatReleaseDate));

        this.tableView.setRowFactory(this::createTableRow);
        this.tableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> detailsPanel.fireEvent(ModInstallerEvent.modSelected(newValue)));
        this.tableView.getColumns().addListener((InvalidationListener) observable -> this.updateModDefinitions());
        this.tableView.getSortOrder().add(this.columnName);

        this.comboBoxFilter.setButtonCell(new ModDefinitionFilterListCell());

        this.comboBoxFilter.getItems().clear();
        this.comboBoxFilter.getItems().add(new ModDefinitionFilter("All", definition -> true));
        this.comboBoxFilter.getItems().add(new ModDefinitionFilter("Compatible", definition -> !this.isIncompatible(definition)));
        this.comboBoxFilter.getItems().add(new ModDefinitionFilter("Installed", definition -> this.isInstalled(definition)));
        this.comboBoxFilter.getItems().add(new ModDefinitionFilter("Updateable", definition -> this.isUpdateAvailable(definition)));
        this.comboBoxFilter.getItems().add(new ModDefinitionFilter("Incompatible", definition -> this.isIncompatible(definition)));

        this.comboBoxFilter.valueProperty().addListener((InvalidationListener) observable -> this.updateModDefinitions());
        this.comboBoxFilter.setValue(this.comboBoxFilter.getItems().get(1));
    }

    private void initializeModInstaller() {
        ModInstallerUI.startAutoCloseProgressDialog("Initializing", this.detailsPane, this.modInstaller::initialize, this::postInitialize);
    }

    @FXML
    private void openLogFolder() {
        Path folder = Paths.get(System.getProperty("user.home"));

        if (OsUtils.isWindows()) {
            folder = folder.resolve("AppData/LocalLow/Hinterland/TheLongDark");
        } else if (OsUtils.isMac()) {
            folder = folder.resolve("Library/Logs/Unity");
        } else {
            folder = folder.resolve(".config/unity3d/Hinterland/TheLongDark");
        }

        try {
            Desktop.getDesktop().open(folder.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void postInitialize() {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(this::postInitialize);
            return;
        }

        ModInstallerUI.setTitle("TLD Mod-Installer " + ModInstaller.VERSION + " (TLD Version: " + this.modInstaller.getTldVersion() + ")");

        if (this.modInstaller.isNewVersionAvailable()) {
            this.askDownloadNewVersion();
        }

        if (this.modInstaller.areOtherVersionsPresent()) {
            this.askDeleteOtherVersions();
        }
    }

    @FXML
    private void refreshSources() {
        ModInstallerUI.startProgressDialog("Refreshing Sources", this.detailsPane, () -> this.modInstaller.refreshSources());
    }

    private void startUpdate() {
        if (!this.modInstaller.hasDownloadedNewVersion()) {
            return;
        }

        try {
            this.modInstaller.startUpdate();
            System.exit(0);
        } catch (Exception e) {
            showError("Could Not Start",
                    "The downloaded version could not be started: " + e.getMessage() + "\n\nYou should try to start it manually.");
        }
    }

    private void updateModDefinitions() {
        Platform.runLater(() -> {
            this.tableView.getItems().setAll(this.getModDefinitions(this.comboBoxFilter.getValue()));
            this.tableView.sort();
            this.tableView.refresh();

            for (int i = 0; i < this.comboBoxFilter.getItems().size(); i++) {
                ModDefinitionFilter element = this.comboBoxFilter.getItems().get(i);
                element.updateCount();
                this.comboBoxFilter.getItems().set(i, element);
            }
        });
    }

    protected class ModDefinitionFilterListCell extends ListCell<ModDefinitionFilter> {

        public ModDefinitionFilterListCell() {
            this.setGraphic(new ImageView(new Image(this.getClass().getResourceAsStream("/baseline_filter_list_black_24x24.png"))));
        }

        public void update() {
            this.updateItem(this.getItem(), this.isEmpty());
        }

        @Override
        protected void updateItem(ModDefinitionFilter item, boolean empty) {
            super.updateItem(item, empty);

            if (!empty) {
                this.setText(item.toString());
            }
        }
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

            StringBuilder stringBuilder = new StringBuilder();

            ModDefinition modDefinition = (ModDefinition) this.getTableRow().getItem();
            if (modDefinition != null) {
                boolean updateAvailable = InstallerMainPanelController.this.isUpdateAvailable(modDefinition);
                this.pseudoClassStateChanged(PSEUDO_CLASS_UPDATE_AVAILABLE, updateAvailable);
                if (updateAvailable) {
                    stringBuilder.append("An newer version is available.");
                }

                boolean required = InstallerMainPanelController.this.isRequired(modDefinition);
                this.pseudoClassStateChanged(PSEUDO_CLASS_REQUIRED, required);
                if (required) {
                    stringBuilder.append("\nCannot be uninstalled because it is required by another mod.");
                }

                boolean recent = InstallerMainPanelController.this.isRecent(modDefinition);
                this.pseudoClassStateChanged(PSEUDO_CLASS_RECENT, recent);
                if (recent) {
                    stringBuilder.append("\nThis version was added or updated recently.");
                }

                boolean incompatible = InstallerMainPanelController.this.isIncompatible(modDefinition);
                this.pseudoClassStateChanged(PSEUDO_CLASS_INCOMPATIBLE, incompatible);
                if (incompatible) {
                    stringBuilder.append("\nThis version is probably incompatible with your current version of TLD.");
                }
            }

            if (stringBuilder.length() == 0) {
                this.setTooltip(null);
            } else {
                this.setTooltip(new Tooltip(stringBuilder.toString().trim()));
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

    private class ModDefinitionFilter implements Predicate<ModDefinition> {

        private final String name;
        private final Predicate<ModDefinition> predicate;
        private int count;

        public ModDefinitionFilter(String name, Predicate<ModDefinition> predicate) {
            super();
            this.name = name;
            this.predicate = predicate;
        }

        @Override
        public boolean test(ModDefinition t) {
            return this.predicate.test(t);
        }

        @Override
        public String toString() {
            return this.name + " (" + this.count + ")";
        }

        public void updateCount() {
            this.count = InstallerMainPanelController.this.getModDefinitions(this).size();
        }
    }
}
