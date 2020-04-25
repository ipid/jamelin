package me.ipid.jamelin.compiler;

import lombok.NonNull;
import me.ipid.jamelin.ast.Ast.*;
import me.ipid.jamelin.constant.PromelaLanguage.BinaryOp;
import me.ipid.jamelin.constant.PromelaLanguage.UnaryOp;
import me.ipid.jamelin.entity.CompileTimeInfo;
import me.ipid.jamelin.entity.il.*;
import me.ipid.jamelin.entity.sa.SAPrimitiveType;
import me.ipid.jamelin.entity.sa.SATypeFactory.PrimitiveTypesLib;
import me.ipid.jamelin.entity.sa.SATypedExpr;
import me.ipid.jamelin.entity.sa.SATypedSlot;
import me.ipid.jamelin.exception.CompileExceptions.NotSupportedException;
import me.ipid.jamelin.exception.CompileExceptions.SyntaxException;
import me.ipid.util.lateinit.LateInit;
import me.ipid.util.visitor.SubclassVisitor;

import java.util.Objects;
import java.util.Optional;

public class ExprConverter {

    public static SATypedExpr buildExpr(@NonNull CompileTimeInfo cInfo, @NonNull AstExpr astExpr) {
        LateInit<SATypedExpr> result = new LateInit<>();

        SubclassVisitor.visit(
                astExpr
        ).when(AstBinaryExpr.class, x -> {
            result.set(buildBinaryExpr(cInfo, x));
        }).when(AstUnaryExpr.class, x -> {
            result.set(buildUnaryExpr(cInfo, x));
        }).when(AstConstExpr.class, x -> {
            result.set(wrapInt(new ILConstExpr(x.num)));
        }).when(AstVarRefExpr.class, x -> {
            result.set(buildVarRefExpr(cInfo, x));
        }).when(AstTernaryExpr.class, x -> {
            result.set(buildTernaryExpr(cInfo, x));
        }).when(AstRunExpr.class, x -> {
            result.set(buildRunExpr(cInfo, x));
        }).other(x -> {
            throw new NotSupportedException("暂不支持 " + x.getClass().getSimpleName() + " 表达式");
        });

        return result.get();
    }

    private static SATypedExpr buildRunExpr(CompileTimeInfo cInfo, AstRunExpr x) {
        if (!x.args.isEmpty()) {
            throw new NotSupportedException("暂不支持带参数运行进程");
        }
        if (x.priority.isPresent()) {
            throw new NotSupportedException("暂不支持带优先级运行进程");
        }

        Optional<ILNamedItem> item = cInfo.nItems.getItem(x.procName);
        if (item.isEmpty()) {
            throw new SyntaxException("不存在名为 " + x.procName + " 的进程，无法 run 该进程");
        } else if (!(item.get() instanceof ILProctype)) {
            throw new SyntaxException(x.procName + " 不是进程，不能 run");
        }

        return wrapInt(new ILRunExpr(cInfo.nItems.getSerialNumOfProc(x.procName)));
    }

    private static SATypedExpr buildBinaryExpr(CompileTimeInfo cInfo, AstBinaryExpr binary) {
        BinaryOp op = Objects.requireNonNull(BinaryOp.fromText.get(binary.op));
        ILExpr a = buildExpr(cInfo, binary.a).expr, b = buildExpr(cInfo, binary.b).expr;

        return wrapInt(new ILBinaryExpr(a, b, op));
    }

    private static SATypedExpr buildTernaryExpr(CompileTimeInfo cInfo, AstTernaryExpr x) {
        return wrapInt(new ILTernaryExpr(
                buildExpr(cInfo, x.cond).expr,
                buildExpr(cInfo, x.ifTrue).expr,
                buildExpr(cInfo, x.ifFalse).expr
        ));
    }

    private static SATypedExpr buildUnaryExpr(CompileTimeInfo cInfo, AstUnaryExpr unary) {
        UnaryOp op = Objects.requireNonNull(UnaryOp.fromText.get(unary.op));
        return wrapInt(new ILUnaryExpr(op, buildExpr(cInfo, unary.target).expr));
    }

    private static SATypedExpr buildVarRefExpr(CompileTimeInfo cInfo, AstVarRefExpr vRef) {
        SATypedSlot slot = VarRefConverter.buildTypedSlotOfVarRef(cInfo, vRef.vRef);

        return new SATypedExpr(slot.type, new ILGetDynMemExpr(
                slot.global,
                slot.combineOffset()
        ));
    }

    private static SATypedExpr wrapInt(ILExpr expr) {
        return new SATypedExpr(PrimitiveTypesLib.int_t, expr);
    }
}
