package me.ipid.jamelin.compiler;

import com.google.common.collect.Lists;
import lombok.NonNull;
import me.ipid.jamelin.ast.Ast.*;
import me.ipid.jamelin.entity.CompileTimeInfo;
import me.ipid.jamelin.entity.RuntimeInfo;
import me.ipid.jamelin.entity.il.*;
import me.ipid.jamelin.entity.sa.SATypeFactory;
import me.ipid.jamelin.entity.state.StateNode;
import me.ipid.jamelin.entity.state.StateUtil;
import me.ipid.jamelin.exception.CompileExceptions.NotSupportedException;
import me.ipid.jamelin.exception.CompileExceptions.SyntaxException;
import me.ipid.util.lateinit.LateInit;
import me.ipid.util.tupling.Tuple2;
import me.ipid.util.visitor.SubclassVisitor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * 职责：AstModule、AstStatement
 */
public class ModuleConverter {

    private static Logger log = LogManager.getLogger(ModuleConverter.class);

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

        // 处理 utype
        // 注：最好先处理 utype 再处理全局变量，否则无法定义 utype 类型的全局变量
        for (var astUtype : program.utypes) {
            log.debug("添加新的自定义类型 <" + astUtype.newTypeName + ">");
            DeclareConverter.addUtype(cInfo, astUtype);
        }

        // 处理全局变量（并收集初始化全局变量的语句）
        for (AstDeclare declare : program.declares) {
            rInfo.initStatements.addAll(DeclareConverter.buildFromDeclare(cInfo, declare));
        }

        // 处理 init 进程
        if (program.init.isPresent()) {
            log.debug("编译 init 进程");
            AstProctype astInit = program.init.get();
            var buildResult = buildProctype(cInfo, astInit);

            // 断定 init 进程必须得是 active 的
            assert buildResult.b;

            addILProctype(rInfo, buildResult.a, true);
        }

        for (AstProctype astProc : program.procs) {
            log.debug("编译 <" + astProc.name + "> 进程");
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
        cInfo.checkNameExist(astProc.name);

        if (astProc.enabler.isPresent()) {
            throw new NotSupportedException("目前暂不支持进程的 enabler");
        } else if (astProc.args.size() > 0) {
            throw new NotSupportedException("目前暂不支持带参数的进程");
        } else if (astProc.priority.isPresent()) {
            throw new NotSupportedException("目前暂不支持带优先级的进程");
        }

        var ilProc = new ILProctype(astProc.name);

        // 构建状态机图，并处理变量声明（会被放入 cInfo 中）
        // 在 handleStatementsBlock 函数中会进入作用域，不必在此处理作用域
        StateNode newEnd = new StateNode();
        ilProc.machine.setEnd(newEnd);
        handleStatementsBlock(cInfo, astProc.statements, ilProc.getStart(), newEnd);

        // 计算进程作用域内的变量总长度，填入进程中
        cInfo.table.fillHistoryLocalMemory(ilProc.memory);
        cInfo.table.resetLocal();

        // 将进程对象放入符号表中
        cInfo.nItems.putProctype(astProc.name, ilProc);

        return Tuple2.of(ilProc, astProc.active);
    }

    private static BuildStatementResult handleAssignment(
            CompileTimeInfo cInfo, AstAssignment assign, StateNode start, StateNode end
    ) {
        StateUtil.link(start, end, AssignConverter.buildAssign(cInfo, assign));
        return new BuildStatementResult(false);
    }

    private static BuildStatementResult handleDeclareStatement(
            CompileTimeInfo cInfo, AstDeclareStatement declares, StateNode start, StateNode end
    ) {
        List<ILStatement> result = new ArrayList<>();

        for (AstDeclare declare : declares.declares) {
            result.addAll(DeclareConverter.buildFromDeclare(cInfo, declare));
        }

        StateUtil.link(start, end, result);
        return new BuildStatementResult(false);
    }

    private static BuildStatementResult handlePrintfStatement(
            CompileTimeInfo cInfo, AstPrintfStatement printf, StateNode start, StateNode end
    ) {
        List<ILExpr> exprList = new ArrayList<>();
        for (AstExpr astExpr : printf.args) {
            exprList.add(ExprConverter.buildExpr(cInfo, astExpr).requirePrimitive());
        }

        var ilPrintf = new ILPrintf(printf.template, exprList);
        StateUtil.link(start, end, Lists.newArrayList(ilPrintf));

        return new BuildStatementResult(false);
    }

    private static BuildStatementResult handleStatement(
            CompileTimeInfo cInfo, AstStatement astStatement, StateNode start, StateNode end
    ) {
        LateInit<BuildStatementResult> result = new LateInit<>();

        SubclassVisitor.visit(
                astStatement
        ).when(AstDeclareStatement.class, x -> {
            result.set(handleDeclareStatement(cInfo, x, start, end));
        }).when(AstPrintfStatement.class, x -> {
            result.set(handlePrintfStatement(cInfo, x, start, end));
        }).when(AstAssignment.class, x -> {
            result.set(handleAssignment(cInfo, x, start, end));
        }).when(AstStatementsBlock.class, x -> {
            result.set(handleStatementsBlock(cInfo, x.statements, start, end));
        }).when(AstAssertStatement.class, x -> {
            result.set(handleAssertStatement(cInfo, x, start, end));
        }).when(AstBlockableStatement.class, x -> {
            result.set(handleBlockableStatement(cInfo, x, start, end));
        }).when(AstIfDoStatement.class, x -> {
            result.set(handleIfDoStatement(cInfo, x, start, end));
        }).when(AstBreakStatement.class, x -> {
            result.set(handleBreakStatement(cInfo, x, start, end));
        }).when(AstSendStatement.class, x -> {
            result.set(handleSendStatement(cInfo, x, start, end));
        }).when(AstRecvStatement.class, x -> {
            result.set(handleRecvStatement(cInfo, x, start, end));
        }).other(x -> {
            throw new NotSupportedException("暂不支持 " + x.getClass().getSimpleName() + " 语句类型");
        });

        return result.get();
    }

    private static BuildStatementResult handleRecvStatement(
            CompileTimeInfo cInfo, AstRecvStatement recv, StateNode start, StateNode end
    ) {
        var stmtCond = ChanConverter.buildRecvStatement(cInfo, recv);
        StateUtil.linkBlocking(start, end, stmtCond.a, stmtCond.b);
        return new BuildStatementResult(false);
    }

    private static BuildStatementResult handleSendStatement(
            CompileTimeInfo cInfo, AstSendStatement send, StateNode start, StateNode end
    ) {
        var stmtCond = ChanConverter.buildSendStatement(cInfo, send);
        StateUtil.linkBlocking(start, end, stmtCond.a, stmtCond.b);
        return new BuildStatementResult(false);
    }

    private static BuildStatementResult handleBreakStatement(
            CompileTimeInfo cInfo, AstBreakStatement breakStatement, StateNode start, StateNode end
    ) {
        if (cInfo.loopExit.isEmpty()) {
            throw new SyntaxException("目前不在 do 循环中，无法使用 break 语句");
        }
        StateNode exit = cInfo.loopExit.peek();

        StateUtil.link(start, exit, new ArrayList<>());
        return new BuildStatementResult(true);
    }

    private static BuildStatementResult handleAssertStatement(
            CompileTimeInfo cInfo, AstAssertStatement assertStatement, StateNode start, StateNode end
    ) {
        StateUtil.link(start, end, Lists.newArrayList(
                new ILAssertStatement(
                        ExprConverter.buildExpr(cInfo, assertStatement.toBeTrue).requirePrimitive(),
                        "断言条件不符合")
                )
        );

        return new BuildStatementResult(false);
    }

    private static BuildStatementResult handleBlockableStatement(
            CompileTimeInfo cInfo, AstBlockableStatement blockable, StateNode start, StateNode end
    ) {
        ILExpr cond = ExprConverter.buildExpr(cInfo, blockable.expr).requirePrimitive();
        StateUtil.linkWithCond(start, end, cond);
        return new BuildStatementResult(false);
    }

    private static BuildStatementResult handleIfDoStatement(
            CompileTimeInfo cInfo, AstIfDoStatement ifDoStatement, StateNode start, StateNode end
    ) {
        // 决定语句的结尾是哪个节点
        // 如果是循环，就要让语句最后回到 start 节点
        StateNode target;
        if (ifDoStatement.isDo) {
            // 记录当前循环的出口，供 break 语句使用
            cInfo.loopExit.push(end);
            target = start;
        } else {
            target = end;
        }

        for (List<AstStatement> choice : ifDoStatement.choices) {
            handleStatementsBlock(cInfo, choice, start, target);
        }

        if (ifDoStatement.isDo) {
            // 如果是循环，那就删掉循环出口
            cInfo.loopExit.pop();
        }

        return new BuildStatementResult(false);
    }

    private static BuildStatementResult handleStatementsBlock(
            CompileTimeInfo cInfo, List<AstStatement> astStatements, StateNode start, StateNode end
    ) {
        if (astStatements.size() <= 0) {
            return new BuildStatementResult(false);
        }

        // 处理语句块带来的作用域
        cInfo.table.enterScope();

        // 使用匿名函数，方便处理 break 语句的逻辑
        Runnable anonymous = () -> {
            StateNode last = start;

            // 遍历 astStatement 列表中，除去最后一条语句以外的语句
            for (int i = 0; i < astStatements.size() - 1; i++) {
                StateNode newEnd = new StateNode();
                var result = handleStatement(cInfo, astStatements.get(i), last, newEnd);
                if (result.breaked) {
                    return;
                }
                last = newEnd;
            }

            // 处理最后一条语句
            handleStatement(cInfo, astStatements.get(astStatements.size() - 1), last, end);
        };
        anonymous.run();

        // 退出作用域
        cInfo.table.exitScope();
        return new BuildStatementResult(false);
    }

    public static final class BuildStatementResult {
        // 当前语句块是否要被 break 掉
        public final boolean breaked;

        public BuildStatementResult(boolean breaked) {
            this.breaked = breaked;
        }
    }
}
