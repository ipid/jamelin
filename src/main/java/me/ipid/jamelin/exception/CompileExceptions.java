package me.ipid.jamelin.exception;

public class CompileExceptions {

    public static class CompileException extends RuntimeException {
        public CompileException(String message) {
            super(message);
        }
    }

    public static class NotSupportedException extends CompileException {
        public NotSupportedException(String message) {
            super(message);
        }
    }

    public static class OutOfLimitException extends CompileException {
        public OutOfLimitException(String message) {
            super("超出限制错误：" + message);
        }
    }

    public static class SyntaxException extends CompileException {
        public SyntaxException(String message) {
            super("语法错误：" + message);
        }
    }

}
