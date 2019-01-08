package me.wulfmarius.modinstaller;

public class AbortException extends ModInstallerException {

    private static final long serialVersionUID = 1L;

    public AbortException(String message) {
        super(message);
    }

    public AbortException(String message, Throwable cause) {
        super(message, cause);
    }
}
