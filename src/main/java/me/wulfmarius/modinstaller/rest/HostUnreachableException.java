package me.wulfmarius.modinstaller.rest;

import me.wulfmarius.modinstaller.AbortException;

public class HostUnreachableException extends AbortException {

    private static final long serialVersionUID = 1L;

    public HostUnreachableException(String host) {
        super("Unknown host " + host + ". Are you offline?");
    }
}
