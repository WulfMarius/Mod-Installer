package me.wulfmarius.modinstaller.repository;

public class SourceException extends RepositoryException {

    private static final long serialVersionUID = 1L;

    public SourceException(String message) {
        super(message);
    }

    public SourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
