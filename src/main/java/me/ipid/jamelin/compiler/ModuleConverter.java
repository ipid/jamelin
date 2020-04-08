package me.ipid.jamelin.compiler;

import com.google.common.collect.Lists;
import me.ipid.jamelin.ast.Ast.*;
import me.ipid.jamelin.entity.CompileTimeInfo;
import me.ipid.jamelin.entity.RuntimeInfo;
import me.ipid.jamelin.entity.il.ILExpr;
import me.ipid.jamelin.entity.il.ILPrintf;
import me.ipid.jamelin.entity.il.ILProctype;
import me.ipid.jamelin.entity.il.ILStatement;
import me.ipid.jamelin.exception.CompileExceptions.NotSupportedException;
import me.ipid.jamelin.util.MemoryUtil;
import me.ipid.util.tupling.Tuple2;
import me.ipid.util.visitor.SubclassVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * 职责：AstModule、AstStatement
 */
public class ModuleConverter {

    public static RuntimeInfo buildRuntimeInfo(AstProgram program) {
        var cInfo = new CompileTimeInfo();
        var rInfo = new RuntimeInfo();

        if (!program.declares.isEmpty()) {
            throw new NotSupportedException("暂不支持全局变量");
        }
        if (!program.inlines.isEmpty()) {
            throw new NotSupportedException("暂不支持 inline 语句块");
        }
        if (!program.mtypes.isEmpty()) {
            throw new NotSupportedException("暂不支持定义 mtype");
        }
        if (!program.utypes.isEmpty()) {
            throw new NotSupportedException("暂不支持定义自定义类型（utype）");
        }

        if (program.init.isPresent()) {
            AstProctype astInit = program.init.get();
            var buildResult = buildProctype(cInfo, astInit);
            assert buildResult.b;

            addILProctype(rInfo, buildResult.a, true);
        }

        for (AstProctype astProc: program.procs) {
            var buildResult = buildProctype(cInfo, astProc);
            addILProctype(rInfo, buildResult.a, buildResult.b);
        }

        cInfo.table.fillMemoryLayout(rInfo.globalMemoryLayout, true);
        return rInfo;
    }

    private static void addILProctype(
            RuntimeInfo rInfo,
            ILProctype proc, boolean active
    ) {
        rInfo.procs.add(proc);
        if (active) {
            rInfo.activeProcs.add(proc);
        }
    }

    private static Tuple2<ILProctype, Boolean> buildProctype(
            CompileTimeInfo cInfo,
            AstProctype astProc
    ) {
        if (astProc.enabler.isPresent()) {
            throw new NotSupportedException("目前暂不支持进程的 enabler");
        } else if (astProc.args.size() > 0) {
            throw new NotSupportedException("目前暂不支持带参数的进程");
        } else if (astProc.priority.isPresent()) {
            throw new NotSupportedException("目前暂不支持带优先级的进程");
        }

        ILProctype ilProc = new ILProctype(astProc.name);

        // 进入作用域
        cInfo.table.enterScope();

        for (AstStatement statement : astProc.statements) {
            handleStatement(cInfo, ilProc, statement);
        }

        // 填写 slot 信息
        cInfo.table.fillMemoryLayout(ilProc.memoryLayout, false);

        // 退出作用域
        cInfo.table.enterScope();
        return Tuple2.of(ilProc, astProc.active);
    }

    private static void handleStatement(
            CompileTimeInfo cInfo, ILProctype ilProc, AstStatement astStatement
    ) {
        SubclassVisitor.visit(
                astStatement
        ).when(AstDeclareStatement.class, x -> {
            handleDeclareStatement(cInfo, ilProc, x);
        }).when(AstPrintfStatement.class, x -> {
            handlePrintfStatement(cInfo, ilProc, x);
        }).other(x -> {
            throw new NotSupportedException("暂不支持 " + x.getClass().getSimpleName() + " 语句类型");
        });
    }

    private static void handleDeclareStatement(
            CompileTimeInfo cInfo, ILProctype ilProc, AstDeclareStatement declares
    ) {
        List<ILStatement> result = new ArrayList<>();

        for (AstDeclare declare : declares.declares) {
            result.addAll(DeclareConverter.buildFromDeclare(cInfo, declare));
        }

        ilProc.stateMachine.linkToNewEnd(result);
    }

    private static void handlePrintfStatement(
            CompileTimeInfo cInfo, ILProctype ilProc, AstPrintfStatement printf
    ) {
        List<ILExpr> exprList = new ArrayList<>();
        for (AstExpr astExpr: printf.args) {
            exprList.add(ExprConverter.buildExpr(cInfo, astExpr));
        }

        var ilPrintf = new ILPrintf(printf.template, exprList);
        ilProc.stateMachine.linkToNewEnd(Lists.newArrayList(ilPrintf));
    }
}
