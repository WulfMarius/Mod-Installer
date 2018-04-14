package me.wulfmarius.modinstaller;

import java.util.List;

public class MissingDependencyException extends ModInstallerException {

    private static final long serialVersionUID = 1L;

    private final List<ModDependency> dependencies;

    public MissingDependencyException(String message, List<ModDependency> dependencies) {
        super(message);
        this.dependencies = dependencies;
    }

    public List<ModDependency> getDependencies() {
        return this.dependencies;
    }

}
