package com.autouncle.avs;

import java.util.List;
import java.util.stream.Collectors;

/** Thrown by {@link Avs#validateOrThrow}; lists every validation error. */
public class InvalidAvsDocumentException extends RuntimeException {
    private final List<ValidationError> errors;

    public InvalidAvsDocumentException(List<ValidationError> errors) {
        super("Invalid AVS document:\n" + errors.stream().map(e -> "  " + e).collect(Collectors.joining("\n")));
        this.errors = List.copyOf(errors);
    }

    public List<ValidationError> errors() {
        return errors;
    }
}
