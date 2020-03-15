package me.ipid.jamelin.compiler;

import me.ipid.jamelin.entity.SymbolicTable;
import me.ipid.jamelin.exception.NotSupportedException;
import me.ipid.jamelin.thirdparty.antlr.PromelaAntlrBaseVisitor;
import me.ipid.jamelin.thirdparty.antlr.PromelaAntlrParser;
import org.antlr.v4.runtime.tree.TerminalNode;

public class ProgramVisitor extends PromelaAntlrBaseVisitor<Void> {

    private SymbolicTable symbolicTable = new SymbolicTable();

    public ProgramVisitor() {

    }

    @Override
    public Void visitModule_Proctype(PromelaAntlrParser.Module_ProctypeContext ctx) {
        return super.visitModule_Proctype(ctx);
    }

    @Override
    public Void visitModule_Init(PromelaAntlrParser.Module_InitContext ctx) {
        throw new NotSupportedException("当前暂不支持 init 语句块");
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
    public Void visitModule_DeclareList(PromelaAntlrParser.Module_DeclareListContext ctx) {
        return super.visitModule_DeclareList(ctx);
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
    public Void visitDelimeter(PromelaAntlrParser.DelimeterContext ctx) {
        // 分隔符没有语法意义，因此不往下 visit
        return null;
    }

    @Override
    public Void visitStatementBlock(PromelaAntlrParser.StatementBlockContext ctx) {
        symbolicTable.enterScope();
        visitChildren(ctx);
        symbolicTable.exitScope();

        return null;
    }

    @Override
    public Void visitOneDeclare_Normal(PromelaAntlrParser.OneDeclare_NormalContext ctx) {
        if (ctx.VISIBLE() != null || ctx.LOCAL() != null) {
            throw new NotSupportedException("目前暂不支持 visible/local 限定符");
        }
        TypeVisitor visitor = new TypeVisitor();
        visitor.visit(ctx.typeName());

        for (TerminalNode id : ctx.IDENTIFIER()) {

        }

        return null;
    }

    @Override
    public Void visitOneDeclare_Unsigned(PromelaAntlrParser.OneDeclare_UnsignedContext ctx) {
        throw new NotSupportedException("目前不支持 unsigned 变量");
    }
}
