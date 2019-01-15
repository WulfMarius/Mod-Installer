package me.wulfmarius.modinstaller;

public class MissingDependencyException extends ModInstallerException {

    private static final long serialVersionUID = 1L;

    private final ModDependencies dependencies;

    public MissingDependencyException(String message, ModDependencies dependencies) {
        super(message);
        this.dependencies = dependencies;
    }

    public ModDependencies getDependencies() {
        return this.dependencies;
    }
}
