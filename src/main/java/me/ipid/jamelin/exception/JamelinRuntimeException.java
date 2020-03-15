package me.ipid.jamelin.exception;

public class JamelinRuntimeException extends RuntimeException {
    public JamelinRuntimeException() {
    }

    public JamelinRuntimeException(String message) {
        super(message);
    }
}
