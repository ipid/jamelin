package me.ipid.jamelin.util;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.List;

public class AntlrErrorListener extends BaseErrorListener {

    List<String> errorList;

    public AntlrErrorListener() {
        this.errorList = new ArrayList<>();
    }

    public List<String> getErrorList() {
        return errorList;
    }

    public boolean isErrorHappened() {
        return errorList.size() > 0;
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
        var builder = new StringBuilder();
        builder.append(String.format("语法错误：第 %d 行，第 %d 字符", line, charPositionInLine));
        if (e != null) {
            String invalid = "";
            Token token = e.getOffendingToken();
            if (token != null) {
                invalid = token.getText();
            }

            if (!invalid.equals("")) {
                builder.append(String.format("：此处的「%s」输入非法", invalid));
            }
        }

        errorList.add(builder.toString());
    }

}
