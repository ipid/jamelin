package me.ipid.jamelin.exception;

public class NotSupportedException extends JamelinRuntimeException {
    public NotSupportedException() {
    }

    public NotSupportedException(String message) {
        super(message);
    }
}
