package me.wulfmarius.modinstaller.repository;

import me.wulfmarius.modinstaller.ModInstallerException;

public class RepositoryException extends ModInstallerException {

    private static final long serialVersionUID = 1L;

    public RepositoryException(String message) {
        super(message);
    }

    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
