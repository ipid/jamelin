package me.ipid.jamelin.compiler;

import com.google.common.collect.Lists;
import me.ipid.jamelin.entity.*;
import me.ipid.jamelin.entity.expr.*;
import me.ipid.jamelin.entity.statement.*;
import me.ipid.jamelin.exception.*;
import me.ipid.jamelin.thirdparty.antlr.*;
import me.ipid.jamelin.thirdparty.antlr.PromelaAntlrParser.*;
import org.apache.commons.text.StringEscapeUtils;

import java.util.ArrayList;
import java.util.List;

public class PrintVisitor extends PromelaAntlrBaseVisitor<List<PromelaStatement>> {
    private CompileTimeInfo info;

    public PrintVisitor(CompileTimeInfo info) {
        this.info = info;
    }

    public List<PromelaStatement> entryPoint(Statement_PrintfContext ctx) {


        String printTemplateRaw = ctx.STRING().getText();
        String printTemplate = StringEscapeUtils.unescapeJava(
                printTemplateRaw.substring(1, printTemplateRaw.length() - 1));
        int templateParamNum = parseTemplate(printTemplate);
        if (exprList.size() != templateParamNum) {
            throw new SyntaxException("printf 模板字符串中，% 参数的个数与传入的参数个数不匹配");
        }

        ArrayList<PromelaStatement> result = new ArrayList<>();
        result.add(new PrintfStatement(printTemplate, exprList));
        return result;
    }

    private int parseTemplate(String printTemplate) {
        int index = 0;
        State state = State.INITIAL;

        for (char c : Lists.charactersOf(printTemplate)) {
            if (state == State.INITIAL) {
                if (c == '%') {
                    state = State.PERCENT;
                }
            } else {
                if (c == 'd') {
                    index++;
                    state = State.INITIAL;
                } else if (c == '%') {
                    state = State.INITIAL;
                } else {
                    throw new Error("暂不支持 %d 以外的 printf 转义字符");
                }
            }
        }

        return index;
    }

    public enum State {
        INITIAL, PERCENT
    }
}
