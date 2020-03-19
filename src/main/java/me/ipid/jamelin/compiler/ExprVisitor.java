package me.ipid.jamelin.compiler;

import me.ipid.jamelin.constant.*;
import me.ipid.jamelin.entity.*;
import me.ipid.jamelin.entity.expr.*;
import me.ipid.jamelin.entity.symbol.*;
import me.ipid.jamelin.exception.*;
import me.ipid.jamelin.thirdparty.antlr.*;
import me.ipid.jamelin.thirdparty.antlr.PromelaAntlrParser.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;

public class ExprVisitor extends PromelaAntlrBaseVisitor<PromelaExpr> {

    private CompileTimeInfo info;
    private CurrVarRef currRef;
    public ExprVisitor(CompileTimeInfo info) {
        this.info = info;
        this.currRef = null;
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
        return visit(ctx.varRef());
    }

    @Override
    public PromelaExpr visitAnyExpr_Binary(AnyExpr_BinaryContext ctx) {
        String opStr = ctx.getChild(TerminalNode.class, 0).getText();
        BinaryOp op = BinaryOp.fromText.get(opStr);

        PromelaExpr left = visit(ctx.anyExpr().get(0)), right = visit(ctx.anyExpr().get(1));
        return new BinaryOpExpr(left, right, op);
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
        return visit(ctx.expr());
    }

    @Override
    public PromelaExpr visitExpr_AnyExpr(Expr_AnyExprContext ctx) {
        return visit(ctx.anyExpr());
    }

    @Override
    public PromelaExpr visitVarRef(VarRefContext ctx) {
        currRef = new CurrVarRef();

        String varName = ctx.IDENTIFIER().getText();
        Optional<SymbolTableItem> varItemRaw = info.getTable().getVar(varName);

        if (!varItemRaw.isPresent()) {
            throw new SyntaxException(String.format("变量 %s 不存在", varName));
        }

        SymbolTableItem item = varItemRaw.get();
        currRef.globalVarRef = item.isGlobal();
        currRef.offset = item.getStartAddr();

        if (ctx.varRef() != null) {
            throw new Error("暂不支持自定义类型");
        }

        return new GetMemExpr(currRef.globalVarRef, currRef.offset);
    }

    private PromelaExpr visitSubVarRef(VarRefContext ctx) {
        throw new Error("TODO：实现自定义类型");
    }

    public List<PromelaExpr> traverseArgList(ArgListContext ctx) {
        List<PromelaExpr> result = new ArrayList<>();

        for (ParseTree tree : ctx.anyExpr()) {
            result.add(visit(tree));
        }

        return result;
    }

    public class CurrVarRef {
        // 当前是否正在处理全局变量
        public boolean globalVarRef = false;
        public int offset = 0;
    }
}
