package me.wulfmarius.modinstaller;

public class ModInstallerException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ModInstallerException(String message) {
        super(message);
    }

    public ModInstallerException(String message, Throwable cause) {
        super(message, cause);
    }
}