package me.ipid.jamelin.compiler;

import com.google.common.collect.Lists;
import lombok.NonNull;
import me.ipid.jamelin.ast.Ast.*;
import me.ipid.jamelin.entity.CompileTimeInfo;
import me.ipid.jamelin.entity.RuntimeInfo;
import me.ipid.jamelin.entity.il.ILExpr;
import me.ipid.jamelin.entity.il.ILPrintf;
import me.ipid.jamelin.entity.il.ILProctype;
import me.ipid.jamelin.entity.il.ILStatement;
import me.ipid.jamelin.entity.sa.SATypeFactory;
import me.ipid.jamelin.exception.CompileExceptions.NotSupportedException;
import me.ipid.util.tupling.Tuple2;
import me.ipid.util.visitor.SubclassVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * 职责：AstModule、AstStatement
 */
public class ModuleConverter {

    public static RuntimeInfo buildRuntimeInfo(@NonNull AstProgram program) {
        var cInfo = new CompileTimeInfo(
                SATypeFactory.getPrimitiveTypeMap());
        var rInfo = new RuntimeInfo();
        if (!program.inlines.isEmpty()) {
            throw new NotSupportedException("暂不支持 inline 语句块");
        }
        if (!program.mtypes.isEmpty()) {
            throw new NotSupportedException("暂不支持定义 mtype");
        }

        // 处理全局变量（并收集初始化全局变量的语句）
        for (AstDeclare declare: program.declares) {
            rInfo.initStatements.addAll(DeclareConverter.buildFromDeclare(cInfo, declare));
        }

        // 处理 utype
        for (var astUtype : program.utypes) {
            DeclareConverter.addUtype(cInfo, astUtype);
        }

        // 处理 init 进程
        if (program.init.isPresent()) {
            AstProctype astInit = program.init.get();
            var buildResult = buildProctype(cInfo, astInit);

            // 断定 init 进程必须得是 active 的
            assert buildResult.b;

            addILProctype(rInfo, buildResult.a, true);
        }

        for (AstProctype astProc : program.procs) {
            var buildResult = buildProctype(cInfo, astProc);
            addILProctype(rInfo, buildResult.a, buildResult.b);
        }

        cInfo.table.fillGlobalMemory(rInfo.globalMemory);
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

        var ilProc = new ILProctype(astProc.name);

        // 进入作用域
        cInfo.table.enterScope();

        for (AstStatement statement : astProc.statements) {
            handleStatement(cInfo, ilProc, statement);
        }

        // 退出作用域
        cInfo.table.exitScope();

        // 计算进程作用域内的变量总长度，填入进程中
        cInfo.table.fillHistoryLocalMemory(ilProc.memory);
        cInfo.table.resetLocal();

        // 将进程对象放入符号表中
        cInfo.nItems.putProctype(astProc.name, ilProc);

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
        }).when(AstAssignment.class, x -> {
            handleAssignment(cInfo, ilProc, x);
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
        for (AstExpr astExpr : printf.args) {
            exprList.add(ExprConverter.buildExpr(cInfo, astExpr).requirePrimitive());
        }

        var ilPrintf = new ILPrintf(printf.template, exprList);
        ilProc.stateMachine.linkToNewEnd(Lists.newArrayList(ilPrintf));
    }

    private static void handleAssignment(
            CompileTimeInfo cInfo, ILProctype ilProc, AstAssignment assign
    ) {
        var exprList = AssignConverter.buildAssign(cInfo, assign);
        ilProc.stateMachine.linkToNewEnd(exprList);
    }
}
