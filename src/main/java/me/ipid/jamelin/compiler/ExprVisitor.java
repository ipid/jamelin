package me.ipid.jamelin.compiler;

import me.ipid.jamelin.entity.code.ImmediateNumExpr;
import me.ipid.jamelin.entity.code.PromelaExpr;
import me.ipid.jamelin.entity.symbol.PromelaNamedItem;
import me.ipid.jamelin.exception.NotSupportedException;
import me.ipid.jamelin.thirdparty.antlr.PromelaAntlrBaseVisitor;
import me.ipid.jamelin.thirdparty.antlr.PromelaAntlrParser.*;

import java.util.Map;

public class ExprVisitor extends PromelaAntlrBaseVisitor<PromelaExpr> {

    private ScopeManager scope;
    private Map<String, PromelaNamedItem> entities;

    public ExprVisitor(ScopeManager scope, Map<String, PromelaNamedItem> entities) {
        this.scope = scope;
        this.entities = entities;
    }

    @Override
    public PromelaExpr visitAnyExpr_Ternary(AnyExpr_TernaryContext ctx) {
        throw new NotSupportedException("暂不支持 AnyExpr_Ternary 语法");
    }

    @Override
    public PromelaExpr visitAnyExpr_Constant(AnyExpr_ConstantContext ctx) {
        ConstExprVisitor visitor = new ConstExprVisitor();
        int num = visitor.visit(ctx.constExpr());
        return new ImmediateNumExpr(num);
    }

    @Override
    public PromelaExpr visitAnyExpr_PredefVar(AnyExpr_PredefVarContext ctx) {
        throw new NotSupportedException("暂不支持 AnyExpr_PredefVar 语法");
    }

    @Override
    public PromelaExpr visitAnyExpr_Enabled(AnyExpr_EnabledContext ctx) {
        throw new NotSupportedException("暂不支持 AnyExpr_Enabled 语法");
    }

    @Override
    public PromelaExpr visitAnyExpr_RemoteRef(AnyExpr_RemoteRefContext ctx) {
        throw new NotSupportedException("暂不支持 AnyExpr_RemoteRef 语法");
    }

    @Override
    public PromelaExpr visitAnyExpr_Compound(AnyExpr_CompoundContext ctx) {
        return visit(ctx.anyExpr());
    }

    @Override
    public PromelaExpr visitAnyExpr_PcValue(AnyExpr_PcValueContext ctx) {
        throw new NotSupportedException("暂不支持 AnyExpr_PcValue 语法");
    }

    @Override
    public PromelaExpr visitAnyExpr_Len(AnyExpr_LenContext ctx) {
        throw new NotSupportedException("暂不支持 AnyExpr_Len 语法");
    }

    @Override
    public PromelaExpr visitAnyExpr_Unary(AnyExpr_UnaryContext ctx) {
        throw new NotSupportedException("暂不支持 AnyExpr_Unary 语法");
    }

    @Override
    public PromelaExpr visitAnyExpr_SetPriority(AnyExpr_SetPriorityContext ctx) {
        throw new NotSupportedException("暂不支持 AnyExpr_SetPriority 语法");
    }

    @Override
    public PromelaExpr visitAnyExpr_Poll(AnyExpr_PollContext ctx) {
        throw new NotSupportedException("暂不支持 AnyExpr_Poll 语法");
    }

    @Override
    public PromelaExpr visitAnyExpr_Run(AnyExpr_RunContext ctx) {
        throw new NotSupportedException("暂不支持 AnyExpr_Run 语法");
    }

    @Override
    public PromelaExpr visitAnyExpr_GetPriority(AnyExpr_GetPriorityContext ctx) {
        throw new NotSupportedException("暂不支持 AnyExpr_GetPriority 语法");
    }

    @Override
    public PromelaExpr visitAnyExpr_VarRef(AnyExpr_VarRefContext ctx) {

    }

    @Override
    public PromelaExpr visitAnyExpr_Binary(AnyExpr_BinaryContext ctx) {
        throw new NotSupportedException("暂不支持 AnyExpr_Binary 语法");
    }

    @Override
    public PromelaExpr visitExpr_Binary(Expr_BinaryContext ctx) {
        throw new NotSupportedException("暂不支持 Expr_Binary 语法");
    }

    @Override
    public PromelaExpr visitExpr_ChanPoll(Expr_ChanPollContext ctx) {
        throw new NotSupportedException("暂不支持 Expr_ChanPoll 语法");
    }

    @Override
    public PromelaExpr visitExpr_Compound(Expr_CompoundContext ctx) {
        throw new NotSupportedException("暂不支持 Expr_Compound 语法");
    }

    @Override
    public PromelaExpr visitExpr_AnyExpr(Expr_AnyExprContext ctx) {
        throw new NotSupportedException("暂不支持 Expr_AnyExpr 语法");
    }
}
