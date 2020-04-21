package me.ipid.jamelin.exception;

import me.ipid.jamelin.exception.CompileExceptions.CompileException;

public class RuntimeExceptions {

    public static class JamelinRuntimeException extends RuntimeException {
        public JamelinRuntimeException(String message) {
            super(message);
        }
    }

}
