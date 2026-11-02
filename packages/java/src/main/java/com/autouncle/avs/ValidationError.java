package com.autouncle.avs;

/**
 * One reason a document is invalid.
 *
 * @param pointer JSON Pointer to the offending value, "" for the document itself
 * @param message what is wrong
 */
public record ValidationError(String pointer, String message) {
    @Override
    public String toString() {
        return (pointer.isEmpty() ? "/" : pointer) + ": " + message;
    }
}
