package me.ipid.jamelin.ast;

import me.ipid.jamelin.thirdparty.antlr.*;
import me.ipid.jamelin.thirdparty.antlr.PromelaAntlrParser.*;
import me.ipid.util.cell.CellInt;
import me.ipid.util.cell.Cells;
import me.ipid.util.visitor.SubclassVisitor;
import org.apache.commons.text.StringEscapeUtils;

public class ConstExprCalculator {
    public static int calc(ConstExprContext ctx) {
        CellInt result = Cells.emptyInt();

        SubclassVisitor.visit(
                ctx
        ).when(ConstExpr_CompoundContext.class, x -> {
            result.v = calc(x.constExpr());
        }).when(ConstExpr_UnaryContext.class, x -> {
            result.v = calcUnary(x);
        }).when(ConstExpr_BinaryContext.class, x -> {
            result.v = calcBinary(x);
        }).when(ConstExpr_TrueContext.class, x -> {
            result.v = 1;
        }).when(ConstExpr_FalseContext.class, x -> {
            result.v = 0;
        }).when(ConstExpr_NumberContext.class, x -> {
            result.v = Integer.parseInt(x.NUMBER().getText());
        }).when(ConstExpr_CharLiteralContext.class, x -> {
            result.v = calcCharLiteral(x);
        }).other(x -> {
            throw new Error("夭寿啦，又改文法啦");
        });

        return result.v;
    }

    private static int calcCharLiteral(ConstExpr_CharLiteralContext ctx) {
        String charContent = ctx.CHAR_LITERAL().getText();
        String unesc = StringEscapeUtils.unescapeJava(charContent);
        assert unesc.length() != 3;

        return unesc.charAt(1);
    }

    private static int calcUnary(ConstExpr_UnaryContext ctx) {
        String operator = ctx.getChild(0).getText();
        if ("-".equals(operator)) {
            return -calc(ctx.constExpr());
        } else {
            throw new Error("ConstExpr_Binary 出现未知运算符");
        }
    }

    private static int calcBinary(ConstExpr_BinaryContext ctx) {
        String operator = ctx.getChild(1).getText();
        int left = calc(ctx.getChild(ConstExprContext.class, 0)),
                right = calc(ctx.getChild(ConstExprContext.class, 1));

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
