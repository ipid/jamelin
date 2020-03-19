package me.ipid.jamelin.compiler;

import me.ipid.jamelin.thirdparty.antlr.*;
import org.apache.commons.text.StringEscapeUtils;

public class ConstExprVisitor extends PromelaAntlrBaseVisitor<Integer> {
    @Override
    public Integer visitConstExpr_False(PromelaAntlrParser.ConstExpr_FalseContext ctx) {
        return 0;
    }

    @Override
    public Integer visitConstExpr_True(PromelaAntlrParser.ConstExpr_TrueContext ctx) {
        return 1;
    }

    @Override
    public Integer visitConstExpr_Number(PromelaAntlrParser.ConstExpr_NumberContext ctx) {
        return Integer.valueOf(ctx.NUMBER().getText());
    }

    @Override
    public Integer visitConstExpr_Compound(PromelaAntlrParser.ConstExpr_CompoundContext ctx) {
        return visit(ctx.constExpr());
    }

    @Override
    public Integer visitConstExpr_CharLiteral(PromelaAntlrParser.ConstExpr_CharLiteralContext ctx) {
        String charContent = ctx.CHAR_LITERAL().getText();
        String unesc = StringEscapeUtils.unescapeJava(charContent);
        assert unesc.length() != 3;

        return (int) unesc.charAt(1);
    }

    @Override
    public Integer visitConstExpr_Unary(PromelaAntlrParser.ConstExpr_UnaryContext ctx) {
        String operator = ctx.getChild(0).getText();
        if ("-".equals(operator)) {
            return -visit(ctx.getChild(PromelaAntlrParser.ConstExprContext.class, 0));
        } else {
            throw new Error("ConstExpr_Binary 出现未知运算符");
        }
    }

    @Override
    public Integer visitConstExpr_Binary(PromelaAntlrParser.ConstExpr_BinaryContext ctx) {
        String operator = ctx.getChild(1).getText();
        int left = visit(ctx.getChild(PromelaAntlrParser.ConstExprContext.class, 0)),
                right = visit(ctx.getChild(PromelaAntlrParser.ConstExprContext.class, 1));

        switch (operator) {
            case "*":
                return left * right;
            case "+":
                return left + right;
            case "-":
                return left - right;
            default:
                throw new Error("ConstExpr_Binary 出现未知运算符");
        }
    }
}