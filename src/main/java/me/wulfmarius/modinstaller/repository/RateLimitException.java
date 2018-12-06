package me.wulfmarius.modinstaller.repository;

import java.time.Instant;

public class RateLimitException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final Instant reset;

    public RateLimitException(Instant reset) {
        super();
        this.reset = reset;
    }

    public Instant getReset() {
        return this.reset;
    }
}
