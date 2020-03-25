package me.ipid.jamelin.ast;

import me.ipid.jamelin.ast.Ast.*;
import me.ipid.jamelin.exception.*;
import me.ipid.jamelin.thirdparty.antlr.*;
import me.ipid.jamelin.thirdparty.antlr.PromelaAntlrParser.*;
import me.ipid.jamelin.util.*;
import me.ipid.util.cell.*;
import me.ipid.util.tupling.Tuple3;
import me.ipid.util.visitor.SubclassVisitor;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.RuleNode;
import org.apache.commons.text.StringEscapeUtils;

import java.util.*;
import java.util.stream.Collectors;

public class BuildAstVisitor extends PromelaAntlrBaseVisitor<AstNode> {
    private List<AstDeclare> buildDeclareList(DeclareListContext ctx) {
        List<AstDeclare> result = new ArrayList<>();

        for (OneDeclareContext oneDeclare : ctx.oneDeclare()) {
            result.addAll(buildOneDeclareAll(oneDeclare));
        }

        return result;
    }

    private List<AstDeclare> buildOneDeclareAll(OneDeclareContext oneDeclare) {
        List<AstDeclare> result = new ArrayList<>();

        SubclassVisitor.visit(
                oneDeclare
        ).when(OneDeclare_NormalContext.class, x -> {
            result.addAll(buildOneDeclareOfNormal(x));
        }).other(x -> {
            throw new NotSupportedException("暂不支持 unsigned 变量的定义");
        });

        return result;
    }

    private List<AstExpr> buildExprsFromArgList(ArgListContext argList) {
        return argList.anyExpr().stream()
                .map(x -> (AstExpr) visit(x))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<AstDeclare> buildOneDeclareOfNormal(OneDeclare_NormalContext ctx) {
        boolean local = ctx.LOCAL() != null;

        boolean show = false, hidden = false;
        if (ctx.VISIBLE() != null) {
            String visibility = ctx.VISIBLE().getText();
            if (visibility.equals("show")) {
                show = true;
            } else if (visibility.equals("hidden")) {
                hidden = true;
            }
        }

        String typeName = ctx.typeName().getText();

        List<AstDeclare> result = new ArrayList<>();
        for (InitVarContext initVar : ctx.initVar()) {
            var info = extractInfoOfInitVar(initVar);
            result.add(new AstDeclare(show, hidden, local, typeName, info.b, info.a, info.c));
        }

        return result;
    }

    private List<AstStatement> buildStatementFromSequence(SequenceContext sequence) {
        return sequence.step().stream()
                .map(x -> ((AstStatement) visit(x)))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private AstVarExprInit buildVarExprInit(InitVar_AnyExprContext ctx) {
        return new AstVarExprInit((AstExpr) visit(ctx.anyExpr()));
    }

    /**
     * 从 InitVarContext 中提取信息。
     *
     * @param ctx InitVarContext 实例
     * @return (数组长度, 变量名, 初值)
     */
    private Tuple3<Integer, String, AstVarInit> extractInfoOfInitVar(InitVarContext ctx) {
        CellInt arrayLen = Cells.of(-1);
        Cell<AstVarInit> initVar = Cells.empty();

        SubclassVisitor.visit(
                ctx
        ).when(InitVar_AnyExprContext.class, x -> {
            initVar.v = buildVarExprInit(x);
        }).other(x -> {
            throw new NotSupportedException("暂不支持 " + x.getClass().getSimpleName() + " 语法");
        });

        return Tuple3.of(arrayLen.v, ctx.varName, initVar.v);
    }

    @Override
    public AstNode visitChildren(RuleNode node) {
        Cell<AstNode> result = Cells.empty();

        SubclassVisitor.visit(
                node
        ).when(ModuleContext.class, x -> {
            result.v = super.visitChildren(x);
        }).other(x -> {
            throw new NotSupportedException("暂不支持 " + node.getClass().getSimpleName() + " 语法");
        });

        return result.v;
    }

    @Override
    public AstProgram visitSpec(SpecContext ctx) {
        List<AstModule> modules = new ArrayList<>();
        Cell<Optional<AstProctype>> init = Cells.of(Optional.empty());

        for (ModuleContext mCtx : ctx.module()) {
            SubclassVisitor.visit(
                    mCtx
            ).when(Module_InitContext.class, x -> {
                init.v = Optional.of(visitInit(x.init()));
            }).other(x -> {
                modules.add((AstModule) visit(x));
            });
        }

        return new AstProgram(modules, init.v);
    }

    @Override
    public AstProctype visitProctype(ProctypeContext ctx) {
        boolean active = false;
        if (ctx.ACTIVE() != null) {
            active = true;
        }

        String name = ctx.IDENTIFIER().getText();

        List<AstDeclare> parameters;
        if (ctx.declareList() != null) {
            parameters = buildDeclareList(ctx.declareList());
        } else {
            parameters = new ArrayList<>();
        }

        Optional<Integer> priority = Optional.empty();
        if (ctx.priority() != null) {
            priority = Optional.of(ConstExprCalculator.calc(ctx.priority().constExpr()));
        }

        Optional<AstExpr> enabler = Optional.empty();
        if (ctx.enabler() != null) {
            enabler = Optional.of((AstExpr) visit(ctx.enabler().expr()));
        }

        AstStatementBlock statementBlock = visitStatementBlock(ctx.statementBlock());

        return new AstProctype(active, name, parameters, priority, enabler, statementBlock);
    }

    @Override
    public AstProctype visitInit(InitContext ctx) {

        Optional<Integer> priority = Optional.empty();
        if (ctx.priority() != null) {
            priority = Optional.of(ConstExprCalculator.calc(ctx.priority().constExpr()));
        }

        AstStatementBlock statements = visitStatementBlock(ctx.statementBlock());

        return new AstProctype(true, "init", new ArrayList<>(), priority, Optional.empty(), statements);
    }

    @Override
    public AstUtype visitUtype(UtypeContext ctx) {
        String name = ctx.IDENTIFIER().getText();
        List<AstDeclare> declares = buildDeclareList(ctx.declareList());

        return new AstUtype(name, declares);
    }

    @Override
    public AstMtype visitMtype(MtypeContext ctx) {
        Optional<String> subType = Optional.empty();
        if (ctx.subType != null) {
            subType = Optional.of(ctx.subType.getText());
        }

        List<String> mtypeName;
        if (ctx.mtypeName != null) {
            mtypeName = ctx.mtypeName.stream()
                    .map(Token::getText)
                    .collect(Collectors.toCollection(ArrayList::new));
        } else {
            mtypeName = new ArrayList<>();
        }

        return new AstMtype(subType, mtypeName);
    }

    @Override
    public AstDeclareStatement visitStatement_OneDeclare(Statement_OneDeclareContext ctx) {
        return new AstDeclareStatement(
                buildOneDeclareAll(ctx.oneDeclare()));
    }

    @Override
    public AstUnlessStatement visitStep_UnlessStatement(Step_UnlessStatementContext ctx) {
        return new AstUnlessStatement(
                (AstStatement) visit(ctx.statement(0)), (AstStatement) visit(ctx.statement(1)));
    }

    @Override
    public AstStatement visitStep_NormalStatement(Step_NormalStatementContext ctx) {
        return (AstStatement) visit(ctx.statement());
    }

    @Override
    public AstStatementBlock visitStatementBlock(StatementBlockContext ctx) {
        return new AstStatementBlock(buildStatementFromSequence(ctx.sequence()));
    }

    @Override
    public AstStatement visitStatement_Compound(Statement_CompoundContext ctx) {
        return new AstStatementBlock(
                buildStatementFromSequence(ctx.statementBlock().sequence()));
    }

    @Override
    public AstStatement visitStatement_Printf(Statement_PrintfContext ctx) {
        String printTemplateRaw = ctx.STRING().getText();
        String printTemplate = StringEscapeUtils.unescapeJava(
                printTemplateRaw.substring(1, printTemplateRaw.length() - 1));

        List<AstExpr> exprList;
        if (ctx.argList() == null) {
            exprList = new ArrayList<>();
        } else {
            exprList = buildExprsFromArgList(ctx.argList());
        }

        int templateParamNum = PromelaPrintfUtil.parseTemplate(printTemplate);
        if (exprList.size() != templateParamNum) {
            throw new SyntaxException("printf 模板字符串中，% 参数的个数与传入的参数个数不匹配");
        }

        return new AstPrintfStatement(printTemplate, exprList);
    }

    @Override
    public AstAssignment visitStatement_Assign(Statement_AssignContext ctx) {
        AssignmentContext assign = ctx.assignment();
        Cell<AstAssignment> result = Cells.empty();

        SubclassVisitor.visit(
                assign
        ).when(Assignment_DummyContext.class, x -> {
        }).when(Assignment_IncreaseContext.class, x -> {
            result.v = new AstAdditionStatement((AstVarRef) visit(x.varRef()), 1);
        }).when(Assignment_DecreaseContext.class, x -> {
            result.v = new AstAdditionStatement((AstVarRef) visit(x.varRef()), -1);
        }).when(Assignment_NormalContext.class, x -> {
            result.v = new AstSetValueStatement((AstVarRef) visit(x.varRef()), (AstExpr) visit(x.anyExpr()));
        });

        return result.v;
    }
}
