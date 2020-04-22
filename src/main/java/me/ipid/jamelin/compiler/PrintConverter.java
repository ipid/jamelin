package me.ipid.jamelin.compiler;

import me.ipid.jamelin.ast.Ast.AstPrintfStatement;
import me.ipid.jamelin.entity.CompileTimeInfo;
import me.ipid.jamelin.entity.il.ILExpr;
import me.ipid.jamelin.entity.il.ILPrintf;
import me.ipid.util.nonnulls.NonNullArrayList;

import java.util.List;

public class PrintConverter {

    public static ILPrintf buildPrintfStatement(
            CompileTimeInfo cInfo, AstPrintfStatement printf
    ) {
        List<ILExpr> args = new NonNullArrayList<>();
        for (var astArg : printf.args) {
            args.add(ExprConverter.buildExpr(cInfo, astArg).requirePrimitive());
        }

        return new ILPrintf(printf.template, args);
    }
}
