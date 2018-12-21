package me.wulfmarius.modinstaller;

import me.wulfmarius.modinstaller.ProgressListener.StepType;

public class ProgressListeners extends Listeners<ProgressListener> {

    public void detail(String detail) {
        this.fire(listener -> listener.stepDetail(detail));
    }

    public void error(String error) {
        this.fire(listener -> listener.stepError(error));
    }

    public void finished() {
        this.finished(null);
    }

    public void finished(String message) {
        this.fire(listener -> listener.finished(message));
    }

    public void started(String name) {
        this.fire(listener -> listener.started(name));
    }

    public void stepProgress(int completed, int total) {
        this.fire(listener -> listener.stepProgress(completed, total));
    }

    public void stepStarted(String step, StepType stepType) {
        this.fire(listener -> listener.stepStarted(step, stepType));
    }
}
