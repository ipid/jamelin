package me.ipid.jamelin.compiler;

import me.ipid.jamelin.ast.Ast.*;
import me.ipid.jamelin.entity.*;
import me.ipid.jamelin.entity.statement.*;
import me.ipid.jamelin.entity.symbol.*;
import me.ipid.jamelin.exception.*;
import me.ipid.jamelin.thirdparty.antlr.*;
import me.ipid.jamelin.thirdparty.antlr.PromelaAntlrParser.*;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.List;

public class ILBuilder {

    public static RuntimeInfo buildRuntimeInfo(AstProgram program) {
        var compileInfo = new CompileTimeInfo();
        var runtimeInfo = new RuntimeInfo();
    }

    public static Proctype buildProctype(AstProctype astProctype) {
        if (astProctype.)
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
    public Void visitStatement_Printf(Statement_PrintfContext ctx) {
        info.getCurrMachine().linkToNewEnd(new PrintVisitor(info).entryPoint(ctx));
        return null;
    }


    private void traverseOneDeclare(OneDeclareContext ctx, DeclareVisitor visitor) {
        List<PromelaStatement> statements = visitor.visit(ctx);
        info.getCurrMachine().linkToNewEnd(statements);
    }
}
