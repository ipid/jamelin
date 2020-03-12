package me.ipid.jamelin.compiler;

import me.ipid.jamelin.entity.SymbolicTable;
import me.ipid.jamelin.exception.NotSupportedException;
import me.ipid.jamelin.thirdparty.antlr.PromelaBaseVisitor;
import me.ipid.jamelin.thirdparty.antlr.PromelaParser;
import org.antlr.v4.runtime.tree.TerminalNode;

public class ProgramVisitor extends PromelaBaseVisitor<Void> {

    private SymbolicTable symbolicTable = new SymbolicTable();

    @Override
    public Void visitModule_Proctype(PromelaParser.Module_ProctypeContext ctx) {
        return super.visitModule_Proctype(ctx);
    }

    @Override
    public Void visitModule_Init(PromelaParser.Module_InitContext ctx) {
        throw new NotSupportedException("当前暂不支持 init 语句块");
    }

    @Override
    public Void visitModule_Never(PromelaParser.Module_NeverContext ctx) {
        throw new NotSupportedException("当前暂不支持 never 语句块");
    }

    @Override
    public Void visitModule_Trace(PromelaParser.Module_TraceContext ctx) {
        throw new NotSupportedException("当前暂不支持 trace/notrace 语句块");
    }

    @Override
    public Void visitModule_Utype(PromelaParser.Module_UtypeContext ctx) {
        throw new NotSupportedException("当前暂不支持自定义结构体");
    }

    @Override
    public Void visitModule_Mtype(PromelaParser.Module_MtypeContext ctx) {
        throw new NotSupportedException("当前暂不支持自定义 mtype");
    }

    @Override
    public Void visitModule_DeclareList(PromelaParser.Module_DeclareListContext ctx) {
        return super.visitModule_DeclareList(ctx);
    }

    @Override
    public Void visitModule_Inline(PromelaParser.Module_InlineContext ctx) {
        throw new NotSupportedException("当前暂不支持 inline");
    }

    @Override
    public Void visitModule_Ltl(PromelaParser.Module_LtlContext ctx) {
        throw new NotSupportedException("当前暂不支持 ltl 语句块");
    }

    @Override
    public Void visitDelimeter(PromelaParser.DelimeterContext ctx) {
        // 分隔符没有语法意义，因此不往下 visit
        return null;
    }

    @Override
    public Void visitStatementBlock(PromelaParser.StatementBlockContext ctx) {
        symbolicTable.enterScope();
        visitChildren(ctx);
        symbolicTable.exitScope();

        return null;
    }

    @Override
    public Void visitOneDeclare_Normal(PromelaParser.OneDeclare_NormalContext ctx) {
        if (ctx.VISIBLE() != null || ctx.LOCAL() != null) {
            throw new NotSupportedException("目前暂不支持 visible/local 限定符");
        }
        String typeName = ctx.typeName();



        for (TerminalNode id : ctx.IDENTIFIER()) {
            symbolicTable.newVariable(
                    id.getText()
            );
        }

        return null;
    }

    @Override
    public Void visitOneDeclare_Unsigned(PromelaParser.OneDeclare_UnsignedContext ctx) {
        throw new NotSupportedException("目前不支持 unsigned 变量");
    }
}
