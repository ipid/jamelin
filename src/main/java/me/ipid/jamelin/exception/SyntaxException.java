package me.ipid.jamelin.exception;

public class SyntaxException extends JamelinRuntimeException {
    public SyntaxException() {
    }

    public SyntaxException(String message) {
        super(message);
    }
}
