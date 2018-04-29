package me.wulfmarius.modinstaller.ui;

import static java.text.MessageFormat.format;
import static me.wulfmarius.modinstaller.utils.StringUtils.*;

import java.util.*;

import javafx.application.Platform;
import javafx.beans.property.*;
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

    @FXML
    private Label labelTime;

    private final Clock clock = new Clock();

    public ProgressDialogController(ModInstaller modInstaller) {
        super();
        this.modInstaller = modInstaller;
    }

    @Override
    public void finished(String message) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> this.finished(message));
            return;
        }

        this.appendToLog("FINISHED");
        if (message != null) {
            this.appendToLog(message);
        }

        this.progressBarStep.setProgress(1);
        this.buttonClose.setDisable(false);
        this.clock.stop();
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

        this.clock.start();
    }

    private void appendToLog(String message) {
        this.textAreaLog.appendText(message + "\n");
        this.textAreaLog.setScrollTop(Double.MAX_VALUE);
    }

    @FXML
    private void initialize() {
        this.modInstaller.addProgressListener(this);
        this.labelTime.textProperty().bind(this.clock.formattedTime);

        this.buttonClose.requestFocus();
    }

    @FXML
    private void onClose() {
        this.modInstaller.removeProgressListener(this);
        this.buttonClose.getScene().getWindow().hide();
    }

    protected static class Clock {

        protected final StringProperty formattedTime = new SimpleStringProperty();

        private final Timer timer = new Timer(true);
        private long startTime;
        private boolean running;

        public boolean isRunning() {
            return this.running;
        }

        public void reset() {
            this.startTime = System.currentTimeMillis();
            this.formattedTime.set("");
        }

        public void start() {
            if (this.running) {
                return;
            }

            Platform.runLater(this::reset);

            this.timer.scheduleAtFixedRate(new TimerTask() {

                @Override
                public void run() {
                    if (!Clock.this.isRunning()) {
                        this.cancel();
                        return;
                    }

                    Platform.runLater(Clock.this::update);
                }
            }, 1000, 200);
            this.running = true;
        }

        public void stop() {
            this.running = false;
        }

        protected void update() {
            long elapsed = System.currentTimeMillis() - this.startTime;
            long minutes = elapsed / 60000;
            elapsed %= 60000;
            long seconds = elapsed / 1000;
            this.formattedTime.set(format("{0,number,00}:{1,number,00}", minutes, seconds));
        }
    }
}
