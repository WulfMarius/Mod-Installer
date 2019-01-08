package me.wulfmarius.modinstaller.rest;

import me.wulfmarius.modinstaller.ModInstallerException;

public class RestClientException extends ModInstallerException {

    private static final long serialVersionUID = 1L;

    public RestClientException(String message) {
        super(message);
    }

    public RestClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
