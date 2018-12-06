package me.wulfmarius.modinstaller.ui;

import static me.wulfmarius.modinstaller.ui.BindingsFactory.*;
import static me.wulfmarius.modinstaller.ui.ModInstallerUI.startProgressDialog;

import java.util.List;
import java.util.stream.Collectors;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import me.wulfmarius.modinstaller.*;

public class ModDetailsPanelController {

    private final ModInstaller modInstaller;

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private Label labelName;
    @FXML
    private Label labelVersion;
    @FXML
    private Hyperlink hyperlinkChangelog;
    @FXML
    private Hyperlink hyperlinkURL;
    @FXML
    private Label labelDescription;

    @FXML
    private Node panelRequires;
    @FXML
    private Label labelRequires;

    @FXML
    private Node panelRequiredBy;
    @FXML
    private Label labelRequiredBy;

    @FXML
    private Label infoInstallForbidden;
    @FXML
    private Button buttonInstall;

    @FXML
    private Label infoUpdateForbidden;
    @FXML
    private Button buttonUpdate;

    @FXML
    private Label infoUninstallForbidden;
    @FXML
    private Button buttonUninstall;

    private final RefreshableObjectProperty<ModDefinition> modDefinitionProperty = new RefreshableObjectProperty<>(null);
    private final Property<String> requiresProperty = new RefreshableObjectProperty<>(null);
    private final Property<String> requiredByProperty = new RefreshableObjectProperty<>(null);

    private final BooleanProperty installForbiddenProperty = new SimpleBooleanProperty(false);
    private final Property<Tooltip> installTooltipProperty = new SimpleObjectProperty<>();

    private final BooleanProperty uninstallForbiddenProperty = new SimpleBooleanProperty(false);
    private final Property<Tooltip> uninstallTooltipProperty = new SimpleObjectProperty<>();

    public ModDetailsPanelController(ModInstaller modInstaller) {
        super();
        this.modInstaller = modInstaller;
    }

    @FXML
    private void initialize() {
        this.modInstaller.addInstallationsChangedListener(this::installationsChanged);

        this.anchorPane.addEventHandler(ModInstallerEvent.MOD_SELECTED, this::onModSelected);
        this.anchorPane.visibleProperty().bind(createHasValueBinding(this.modDefinitionProperty));

        this.labelName.textProperty().bind(createModDefinitionNameBinding(this.modDefinitionProperty));
        this.labelVersion.textProperty().bind(createModDefinitionVersionBinding(this.modDefinitionProperty));
        this.hyperlinkChangelog.visitedProperty().bind(new SimpleBooleanProperty(false));
        this.hyperlinkURL.textProperty().bind(createModDefinitionURLBinding(this.modDefinitionProperty));
        this.hyperlinkURL.visitedProperty().bind(new SimpleBooleanProperty(false));
        this.labelDescription.textProperty().bind(createModDefinitionDescriptionBinding(this.modDefinitionProperty));

        this.panelRequires.visibleProperty().bind(createIsNotEmptyBinding(this.requiresProperty));
        this.panelRequires.managedProperty().bind(this.panelRequires.visibleProperty());
        this.labelRequires.textProperty().bind(this.requiresProperty);

        this.panelRequiredBy.visibleProperty().bind(createIsNotEmptyBinding(this.requiredByProperty));
        this.panelRequiredBy.managedProperty().bind(this.panelRequiredBy.visibleProperty());
        this.labelRequiredBy.textProperty().bind(this.requiredByProperty);

        this.buttonInstall.visibleProperty().bind(createModDefinitionInstallBinding(this.modDefinitionProperty, this.modInstaller));
        this.buttonInstall.disableProperty().bind(this.installForbiddenProperty);
        this.infoInstallForbidden.visibleProperty()
                .bind(Bindings.and(this.buttonInstall.visibleProperty(), this.installForbiddenProperty));
        this.infoInstallForbidden.tooltipProperty().bind(this.installTooltipProperty);

        this.buttonUpdate.visibleProperty().bind(createModDefinitionUpdateBinding(this.modDefinitionProperty, this.modInstaller));
        this.buttonUpdate.disableProperty().bind(this.installForbiddenProperty);
        this.infoUpdateForbidden.visibleProperty()
                .bind(Bindings.and(this.buttonUpdate.visibleProperty(), this.installForbiddenProperty));
        this.infoUpdateForbidden.tooltipProperty().bind(this.installTooltipProperty);

        this.buttonUninstall.visibleProperty()
                .bind(createModDefinitionUninstallBinding(this.modDefinitionProperty, this.modInstaller));
        this.buttonUninstall.disableProperty().bind(this.uninstallForbiddenProperty);
        this.infoUninstallForbidden.visibleProperty().bind(this.uninstallForbiddenProperty);
        this.infoUninstallForbidden.tooltipProperty().bind(this.uninstallTooltipProperty);
    }

    private void installationsChanged() {
        this.modDefinitionProperty.fireValueChangedEvent();
    }

    @FXML
    private void installMod() {
        ModDefinition modDefinition = this.modDefinitionProperty.getValue();
        if (modDefinition == null) {
            return;
        }

        startProgressDialog("Installing " + modDefinition.getDisplayName(), this.anchorPane,
                () -> this.modInstaller.install(modDefinition));
    }

    private void onModSelected(ModInstallerEvent event) {
        ModDefinition modDefinition = event.getModDefinition();
        this.modDefinitionProperty.setValue(modDefinition);

        if (modDefinition == null) {
            return;
        }

        try {
            List<ModDefinition> requires = this.modInstaller.getRequires(modDefinition);
            this.requiresProperty.setValue(requires.stream().map(ModDefinition::getName).collect(Collectors.joining(", ")));
            this.installForbiddenProperty.setValue(false);
        } catch (MissingDependencyException e) {
            String message = e.getDependencies().stream().map(ModDependency::getDisplayName)
                    .collect(Collectors.joining(", ", "Requires missing dependencies: ", ""));
            this.requiresProperty.setValue(message);
            this.installForbiddenProperty.setValue(true);
            this.installTooltipProperty.setValue(new Tooltip(message));
        }

        List<ModDefinition> requiredBy = this.modInstaller.getRequiredBy(modDefinition);
        this.requiredByProperty.setValue(requiredBy.stream().map(ModDefinition::getName).collect(Collectors.joining(", ")));

        List<ModDefinition> installedRequiredBy = requiredBy.stream().filter(this.modInstaller::isAnyVersionInstalled)
                .collect(Collectors.toList());
        this.uninstallForbiddenProperty.setValue(!installedRequiredBy.isEmpty());
        this.uninstallTooltipProperty.setValue(new Tooltip(installedRequiredBy.stream().map(ModDefinition::getName)
                .collect(Collectors.joining(", ", "Required by installed mods: ", ""))));
    }

    @FXML
    private void openURL() {
        ModInstallerUI.openURL(this.modDefinitionProperty.get().getUrl());
    }

    @FXML
    private void showChangelog() {
        ModInstallerUI.startChangeLogViewer(this.anchorPane, this.modDefinitionProperty.get());
    }

    @FXML
    private void uninstallMod() {
        ModDefinition modDefinition = this.modDefinitionProperty.getValue();
        if (modDefinition == null) {
            return;
        }

        startProgressDialog("Uninstalling " + modDefinition.getDisplayName(), this.anchorPane,
                () -> this.modInstaller.uninstallAll(modDefinition.getName()));
    }
}
