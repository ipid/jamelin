package me.ipid.jamelin.compiler;

import me.ipid.jamelin.entity.*;
import me.ipid.jamelin.entity.statement.*;
import me.ipid.jamelin.entity.symbol.*;
import me.ipid.jamelin.exception.*;
import me.ipid.jamelin.thirdparty.antlr.*;
import me.ipid.jamelin.thirdparty.antlr.PromelaAntlrParser.*;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.List;

public class ProgramVisitor extends PromelaAntlrBaseVisitor<Void> {

    private CompileTimeInfo info;
    private RuntimeInfo runInfo;

    public ProgramVisitor(CompileTimeInfo info, RuntimeInfo runInfo) {
        this.info = info;
        this.runInfo = runInfo;

        Proctype initProc = runInfo.getInitProc();
        info.getEntities().put("init", initProc);
    }

    @Override
    public Void visitSpec(SpecContext ctx) {
        // 完成整个程序的 visit 工作
        visitChildren(ctx);

        // 填写 global 内存布局
        info.getTable().fillMemoryLayout(runInfo.getGlobalMemoryLayout(), true);

        return null;
    }

    @Override
    public Void visitStatementBlock(StatementBlockContext ctx) {
        // 管理作用域深度

        info.getTable().enterScope();
        visitChildren(ctx);
        info.getTable().exitScope();

        return null;
    }

    @Override
    public Void visitModule_Proctype(PromelaAntlrParser.Module_ProctypeContext ctx) {
        throw new NotSupportedException("当前暂不支持 proctype 语句块");
    }

    @Override
    public Void visitInit(InitContext ctx) {
        if (ctx.priority() != null) {
            throw new NotSupportedException("当前暂不支持设置进程优先级");
        }

        info.setProcName("init");
        visit(ctx.statementBlock());

        // 填写 init 进程专属的内存布局
        info.getTable().fillMemoryLayout(info.getProc().getMemoryLayout(), false);
        return null;
    }

    @Override
    public Void visitModule_Never(PromelaAntlrParser.Module_NeverContext ctx) {
        throw new NotSupportedException("当前暂不支持 never 语句块");
    }

    @Override
    public Void visitModule_Trace(PromelaAntlrParser.Module_TraceContext ctx) {
        throw new NotSupportedException("当前暂不支持 trace/notrace 语句块");
    }

    @Override
    public Void visitModule_Utype(PromelaAntlrParser.Module_UtypeContext ctx) {
        throw new NotSupportedException("当前暂不支持自定义结构体");
    }

    @Override
    public Void visitModule_Mtype(PromelaAntlrParser.Module_MtypeContext ctx) {
        throw new NotSupportedException("当前暂不支持自定义 mtype");
    }

    @Override
    public Void visitDeclareList(DeclareListContext ctx) {
        DeclareVisitor visitor = new DeclareVisitor(info);

        for (ParseTree tree : ctx.oneDeclare()) {
            // 每一个 oneDeclare 都是一条语句
            traverseOneDeclare((OneDeclare_NormalContext) tree, visitor);
        }

        return null;
    }

    @Override
    public Void visitOneDeclare_Normal(OneDeclare_NormalContext ctx) {
        DeclareVisitor visitor = new DeclareVisitor(info);
        traverseOneDeclare(ctx, visitor);
        return null;
    }

    @Override
    public Void visitOneDeclare_Unsigned(OneDeclare_UnsignedContext ctx) {
        DeclareVisitor visitor = new DeclareVisitor(info);
        traverseOneDeclare(ctx, visitor);
        return null;
    }

    @Override
    public Void visitModule_Inline(PromelaAntlrParser.Module_InlineContext ctx) {
        throw new NotSupportedException("当前暂不支持 inline");
    }

    @Override
    public Void visitModule_Ltl(PromelaAntlrParser.Module_LtlContext ctx) {
        throw new NotSupportedException("当前暂不支持 ltl 语句块");
    }

    @Override
    public Void visitStep(StepContext ctx) {
        if (ctx.UNLESS() != null) {
            throw new NotSupportedException("当前暂不支持 unless 语句");
        }

        return visitChildren(ctx);
    }

    @Override
    public Void visitStatement_Xr(Statement_XrContext ctx) {
        throw new Error("暂不支持 Statement_Xr 语法");
    }

    @Override
    public Void visitStatement_Xs(Statement_XsContext ctx) {
        throw new Error("暂不支持 Statement_Xs 语法");
    }

    @Override
    public Void visitStatement_If(Statement_IfContext ctx) {
        throw new Error("暂不支持 Statement_If 语法");
    }

    @Override
    public Void visitStatement_Do(Statement_DoContext ctx) {
        throw new Error("暂不支持 Statement_Do 语法");
    }

    @Override
    public Void visitStatement_For(Statement_ForContext ctx) {
        throw new Error("暂不支持 Statement_For 语法");
    }

    @Override
    public Void visitStatement_Atomic(Statement_AtomicContext ctx) {
        throw new Error("暂不支持 Statement_Atomic 语法");
    }

    @Override
    public Void visitStatement_Dstep(Statement_DstepContext ctx) {
        throw new Error("暂不支持 Statement_Dstep 语法");
    }

    @Override
    public Void visitStatement_Select(Statement_SelectContext ctx) {
        throw new Error("暂不支持 Statement_Select 语法");
    }

    @Override
    public Void visitStatement_Compound(Statement_CompoundContext ctx) {
        throw new Error("暂不支持 Statement_Compound 语法");
    }

    @Override
    public Void visitStatement_Break(Statement_BreakContext ctx) {
        throw new Error("暂不支持 Statement_Break 语法");
    }

    @Override
    public Void visitStatement_Goto(Statement_GotoContext ctx) {
        throw new Error("暂不支持 Statement_Goto 语法");
    }

    @Override
    public Void visitStatement_Labeled(Statement_LabeledContext ctx) {
        throw new Error("暂不支持 Statement_Labeled 语法");
    }

    @Override
    public Void visitStatement_Printf(Statement_PrintfContext ctx) {
        info.getCurrMachine().linkToNewEnd(new PrintVisitor(info).entryPoint(ctx));
        return null;
    }

    @Override
    public Void visitStatement_Printm(Statement_PrintmContext ctx) {
        throw new Error("暂不支持 Statement_Printm 语法");
    }

    @Override
    public Void visitStatement_Assert(Statement_AssertContext ctx) {
        throw new Error("暂不支持 Statement_Assert 语法");
    }

    @Override
    public Void visitStatement_Expr(Statement_ExprContext ctx) {
        throw new Error("暂不支持 Statement_Expr 语法");
    }

    @Override
    public Void visitStatement_Send(Statement_SendContext ctx) {
        throw new Error("暂不支持 Statement_Send 语法");
    }

    @Override
    public Void visitStatement_Receive(Statement_ReceiveContext ctx) {
        throw new Error("暂不支持 Statement_Receive 语法");
    }

    @Override
    public Void visitStatement_CallInline(Statement_CallInlineContext ctx) {
        throw new Error("暂不支持 Statement_CallInline 语法");
    }

    @Override
    public Void visitAssignment_Normal(Assignment_NormalContext ctx) {
        throw new Error("暂不支持 Assignment_Normal 语法");
    }

    @Override
    public Void visitAssignment_Dummy(Assignment_DummyContext ctx) {
        throw new Error("暂不支持 Assignment_Dummy 语法");
    }

    @Override
    public Void visitAssignment_Increase(Assignment_IncreaseContext ctx) {
        throw new Error("暂不支持 Assignment_Increase 语法");
    }

    @Override
    public Void visitAssignment_Decrease(Assignment_DecreaseContext ctx) {
        throw new Error("暂不支持 Assignment_Decrease 语法");
    }

    private void traverseOneDeclare(OneDeclareContext ctx, DeclareVisitor visitor) {
        List<PromelaStatement> statements = visitor.visit(ctx);
        info.getCurrMachine().linkToNewEnd(statements);
    }
}
