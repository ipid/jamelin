package me.ipid.jamelin.compiler;

import org.antlr.v4.runtime.*;

public class ErrorListener extends BaseErrorListener {

    boolean errorHappened = false;

    @Override
    public void syntaxError(
            Recognizer<?, ?> recognizer,
            Object offendingSymbol,
            int line,
            int charPositionInLine,
            String msg,
            RecognitionException e
    ) {
        errorHappened = true;
    }

    public boolean isErrorHappened() {
        return errorHappened;
    }
}
