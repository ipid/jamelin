package me.ipid.jamelin.compiler;

import me.ipid.jamelin.entity.PromelaType;
import me.ipid.jamelin.entity.code.PromelaStateMachine;
import me.ipid.jamelin.exception.NotSupportedException;
import me.ipid.jamelin.thirdparty.antlr.PromelaAntlrBaseVisitor;
import me.ipid.jamelin.thirdparty.antlr.PromelaAntlrParser;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;

public class ProgramVisitor extends PromelaAntlrBaseVisitor<Object> {

    private ScopeManager globalScope, currScope;
    private PromelaStateMachine currProcStateMachine;
    private Map<String, PromelaType> userTypes;

    public ProgramVisitor() {
        globalScope = new ScopeManager();
        currScope = globalScope;

        userTypes = new HashMap<>();
    }

    @Override
    public Object visitModule_Proctype(PromelaAntlrParser.Module_ProctypeContext ctx) {
        throw new NotSupportedException("当前暂不支持 proctype 语句块");
    }

    @Override
    public Object visitModule_Init(PromelaAntlrParser.Module_InitContext ctx) {
        throw new NotSupportedException("当前暂不支持 init 语句块");
    }

    @Override
    public Object visitModule_Never(PromelaAntlrParser.Module_NeverContext ctx) {
        throw new NotSupportedException("当前暂不支持 never 语句块");
    }

    @Override
    public Object visitModule_Trace(PromelaAntlrParser.Module_TraceContext ctx) {
        throw new NotSupportedException("当前暂不支持 trace/notrace 语句块");
    }

    @Override
    public Object visitModule_Utype(PromelaAntlrParser.Module_UtypeContext ctx) {
        throw new NotSupportedException("当前暂不支持自定义结构体");
    }

    @Override
    public Object visitModule_Mtype(PromelaAntlrParser.Module_MtypeContext ctx) {
        throw new NotSupportedException("当前暂不支持自定义 mtype");
    }

    @Override
    public Object visitModule_DeclareList(PromelaAntlrParser.Module_DeclareListContext ctx) {
        DeclareVisitor visitor = new DeclareVisitor(currScope, userTypes);
        visitor.visitChildren(ctx);

        return null;
    }

    @Override
    public Object visitModule_Inline(PromelaAntlrParser.Module_InlineContext ctx) {
        throw new NotSupportedException("当前暂不支持 inline");
    }

    @Override
    public Object visitModule_Ltl(PromelaAntlrParser.Module_LtlContext ctx) {
        throw new NotSupportedException("当前暂不支持 ltl 语句块");
    }
}
