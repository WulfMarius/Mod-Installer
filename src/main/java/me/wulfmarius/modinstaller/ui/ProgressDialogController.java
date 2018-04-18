package me.wulfmarius.modinstaller.ui;

import static me.wulfmarius.modinstaller.utils.StringUtils.*;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import me.wulfmarius.modinstaller.*;

public class ProgressDialogController implements ProgressListener {

    private final ModInstaller modInstaller;

    private String currentStep;
    private StepType currentStepType;

    @FXML
    private Pane pane;

    @FXML
    private Button buttonClose;

    @FXML
    private Label labelStep;

    @FXML
    private ProgressBar progressBarStep;

    @FXML
    private TextArea textAreaLog;

    public ProgressDialogController(ModInstaller modInstaller) {
        super();
        this.modInstaller = modInstaller;
    }

    @Override
    public void finished() {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(this::finished);
            return;
        }

        this.appendToLog("FINISHED");
        this.progressBarStep.setProgress(1);
        this.buttonClose.setDisable(false);
    }

    public void setRunnable(Runnable runnable) {
        WindowBecameVisibleHandler.install(this.pane, runnable);
    }

    @Override
    public void started(String name) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> this.started(name));
            return;
        }

        this.buttonClose.setDisable(true);
        this.buttonClose.getScene().getWindow().setOnCloseRequest(event -> {
            if (this.buttonClose.isDisabled()) {
                event.consume();
            }
        });
    }

    @Override
    public void stepDetail(String detail) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> this.stepDetail(detail));
            return;
        }

        this.appendToLog("\t" + detail);
    }

    @Override
    public void stepProgress(int completed, int total) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> this.stepProgress(completed, total));
            return;
        }

        this.progressBarStep.setProgress((double) completed / total);
        if (this.currentStepType == StepType.DOWNLOAD) {
            this.labelStep.setText(this.currentStepType + " " + shortenPath(this.currentStep) + ": " + formatByteCount(completed) + "/"
                    + formatByteCount(total));
        }
    }

    @Override
    public void stepStarted(String step, StepType stepType) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> this.stepStarted(step, stepType));
            return;
        }

        this.currentStep = step;
        this.currentStepType = stepType;
        this.labelStep.setText(stepType + ": " + step);
        this.appendToLog(stepType + ": " + step);
    }

    private void appendToLog(String message) {
        this.textAreaLog.appendText(message + "\n");
        this.textAreaLog.setScrollTop(Double.MAX_VALUE);
    }

    @FXML
    private void initialize() {
        this.modInstaller.addProgressListener(this);

        this.buttonClose.requestFocus();
    }

    @FXML
    private void onClose() {
        this.modInstaller.removeProgressListener(this);
        this.buttonClose.getScene().getWindow().hide();
    }
}
