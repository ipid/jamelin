package me.ipid.jamelin.compiler;

import org.antlr.v4.runtime.*;

import java.util.ArrayList;
import java.util.List;

public class ErrorListener extends BaseErrorListener {

    List<String> errorList;

    public ErrorListener() {
        this.errorList = new ArrayList<>();
    }

    public List<String> getErrorList() {
        return errorList;
    }

    @Override
    public void syntaxError(
            Recognizer<?, ?> recognizer,
            Object offendingSymbol,
            int line,
            int charPositionInLine,
            String msg,
            RecognitionException e
    ) {
        errorList.add(String.format(
                "语法错误：第 %d 行，第 %d 字符：此处的「%s」输入非法",
                line, charPositionInLine, e.getOffendingToken().getText()
        ));
    }

    public boolean isErrorHappened() {
        return errorList.size() > 0;
    }

}
