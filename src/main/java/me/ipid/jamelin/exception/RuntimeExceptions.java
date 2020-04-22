package me.ipid.jamelin.exception;

public class RuntimeExceptions {

    public static class JamelinRuntimeException extends RuntimeException {
        public JamelinRuntimeException(String message) {
            super(message);
        }
    }

}
