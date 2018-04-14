package me.wulfmarius.modinstaller;

public interface ProgressListener {

    void finished();

    void started(String name);

    void stepDetail(String detail);

    void stepProgress(int completed, int total);

    void stepStarted(String step, StepType stepType);

    enum StepType {
        DOWNLOAD, INSTALL, UNINSTALL, REFRESH, ADD;
    }
}
