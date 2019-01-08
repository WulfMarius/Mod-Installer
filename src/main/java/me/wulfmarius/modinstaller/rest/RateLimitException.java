package me.wulfmarius.modinstaller.rest;

import java.time.*;
import java.time.format.*;

import me.wulfmarius.modinstaller.AbortException;

public class RateLimitException extends AbortException {

    private static final long serialVersionUID = 1L;

    private final Instant reset;

    public RateLimitException(Instant reset) {
        super("RATE LIMIT REACHED. Please try again after "
                + DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM).format(reset.atZone(ZoneId.systemDefault())));
        this.reset = reset;
    }

    public Instant getReset() {
        return this.reset;
    }
}
