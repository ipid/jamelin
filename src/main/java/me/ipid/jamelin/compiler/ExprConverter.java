package me.ipid.jamelin.compiler;

import me.ipid.jamelin.ast.Ast.*;
import me.ipid.jamelin.constant.PromelaLanguage.BinaryOp;
import me.ipid.jamelin.constant.PromelaLanguage.UnaryOp;
import me.ipid.jamelin.entity.CompileTimeInfo;
import me.ipid.jamelin.entity.il.*;
import me.ipid.jamelin.exception.CompileExceptions.NotSupportedException;
import me.ipid.jamelin.exception.CompileExceptions.SyntaxException;
import me.ipid.jamelin.thirdparty.antlr.PromelaAntlrBaseVisitor;
import me.ipid.util.cell.Cell;
import me.ipid.util.cell.Cells;
import me.ipid.util.tupling.Tuple2;
import me.ipid.util.visitor.SubclassVisitor;

import java.util.Objects;

public class ExprConverter extends PromelaAntlrBaseVisitor<ILExpr> {

    public static ILExpr buildExpr(CompileTimeInfo cInfo, AstExpr astExpr) {
        Cell<ILExpr> result = Cells.empty();

        SubclassVisitor.visit(
                astExpr
        ).when(AstBinaryExpr.class, x -> {
            result.v = buildBinaryExpr(cInfo, x);
        }).when(AstUnaryExpr.class, x -> {
            result.v = buildUnaryExpr(cInfo, x);
        }).when(AstConstExpr.class, x -> {
            result.v = new ILConstExpr(x.num);
        }).when(AstVarRefExpr.class, x -> {
            result.v = buildVarRefExpr(cInfo, x);
        }).other(x -> {
            throw new NotSupportedException("暂不支持 " + x.getClass().getSimpleName() + " 表达式");
        });

        return result.v;
    }

    private static ILBinaryExpr buildBinaryExpr(CompileTimeInfo cInfo, AstBinaryExpr binary) {
        BinaryOp op = Objects.requireNonNull(BinaryOp.fromText.get(binary.op));
        ILExpr a = buildExpr(cInfo, binary.a), b = buildExpr(cInfo, binary.b);

        return new ILBinaryExpr(a, b, op);
    }

    private static ILUnaryExpr buildUnaryExpr(CompileTimeInfo cInfo, AstUnaryExpr unary) {
        UnaryOp op = Objects.requireNonNull(UnaryOp.fromText.get(unary.op));
        return new ILUnaryExpr(op, buildExpr(cInfo, unary.target));
    }

    private static ILGetMemExpr buildVarRefExpr(CompileTimeInfo cInfo, AstVarRefExpr varRef) {
        var slot = findSlotOfVarRef(cInfo, varRef);
        return new ILGetMemExpr(slot.a, slot.b);
    }

    // isGlobal, startAddr
    private static Tuple2<Boolean, Integer> findSlotOfVarRef(CompileTimeInfo cInfo, AstVarRefExpr varRef) {
        Cell<Tuple2<Boolean, Integer>> result = Cells.empty();

        SubclassVisitor.visit(
                varRef.vRef
        ).when(AstVarNameAccess.class, x -> {
            var nullableTableItem = cInfo.table.getVar(x.varName);
            if (nullableTableItem.isEmpty()) {
                throw new SyntaxException("变量 " + x.varName + " 不存在");
            }

            var tableItem = nullableTableItem.get();
            result.v = Tuple2.of(tableItem.isGlobal, tableItem.startAddr);
        }).other(x -> {
            throw new NotSupportedException("别整那些高端的，现在不支持：" + x.getClass().getSimpleName());
        });

        return result.v;
    }
}
