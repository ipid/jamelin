package me.ipid.jamelin.ast;

import me.ipid.jamelin.ast.Ast.*;
import me.ipid.jamelin.constant.PromelaLanguage.PredefVar;
import me.ipid.jamelin.exception.CompileExceptions.NotSupportedException;
import me.ipid.jamelin.exception.CompileExceptions.SyntaxException;
import me.ipid.jamelin.thirdparty.antlr.PromelaAntlrBaseVisitor;
import me.ipid.jamelin.thirdparty.antlr.PromelaAntlrParser.*;
import me.ipid.jamelin.util.PromelaPrintfUtil;
import me.ipid.util.cell.Cell;
import me.ipid.util.cell.Cells;
import me.ipid.util.errors.Unreachable;
import me.ipid.util.nonnulls.NonNullArrayList;
import me.ipid.util.tupling.Tuple3;
import me.ipid.util.visitor.SubclassVisitor;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.text.StringEscapeUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class BuildAstVisitor extends PromelaAntlrBaseVisitor<AstNode> {

    public AstProgram buildProgram(SpecContext ctx) {
        List<AstProctype> procs = new NonNullArrayList<>();
        List<AstUtype> utypes = new NonNullArrayList<>();
        List<AstMtype> mtypes = new NonNullArrayList<>();
        List<AstDeclare> declares = new NonNullArrayList<>();
        List<AstInline> inlines = new NonNullArrayList<>();
        Cell<Optional<AstProctype>> init = Cells.of(Optional.empty());

        for (ModuleContext mCtx : ctx.module()) {
            SubclassVisitor.visit(
                    mCtx
            ).when(Module_ProctypeContext.class, x -> {
                procs.add(buildProctype(x.proctype()));
            }).when(Module_InitContext.class, x -> {
                if (init.v.isPresent()) {
                    throw new SyntaxException("init 进程重复定义了");
                }
                init.v = Optional.of(buildInit(x.init()));
            }).when(Module_UtypeContext.class, x -> {
                utypes.add(buildUtype(x.utype()));
            }).when(Module_MtypeContext.class, x -> {
                mtypes.add(buildMtype(x.mtype()));
            }).when(Module_DeclareListContext.class, x -> {
                declares.addAll(buildDeclareList(x.declareList()));
            }).when(Module_InlineContext.class, x -> {
                inlines.add(buildInline(x.inline()));
            }).when(Module_EmptyContext.class, x -> {
            }).other(x -> {
                throw new NotSupportedException("暂不支持 never/trace/ltl 语法");
            });
        }

        return new AstProgram(procs, utypes, mtypes, declares, inlines, init.v);
    }

    private List<AstExpr> buildAnyExprList(List<? extends AnyExprContext> rawExprs) {
        return rawExprs.stream()
                .map(x -> (AstExpr) visit(x))
                .collect(Collectors.toCollection(NonNullArrayList::new));
    }

    private AstChanInit buildChanInit(ChanInitContext ctx) {
        int bufLen = ConstExprCalculator.calc(ctx.constExpr());
        List<String> typeTuple = new NonNullArrayList<>();

        if (ctx.typeName() != null) {
            for (var tp : ctx.typeName()) {
                typeTuple.add(tp.getText());
            }
        }

        if (typeTuple.size() <= 0) {
            throw new SyntaxException("信道不能一个类型也没有");
        }
        return new AstChanInit(bufLen, typeTuple);
    }

    private AstChanPollExpr buildChanPollExpr(PollContext ctx) {
        Cell<AstChanPollExpr> result = Cells.empty();

        SubclassVisitor.visit(
                ctx
        ).when(Poll_FifoContext.class, x -> {
            result.v = new AstChanPollExpr(buildVarRef(x.varRef()), buildRecvArgs(x.recvArgs()), true);
        }).when(Poll_RandomContext.class, x -> {
            result.v = new AstChanPollExpr(buildVarRef(x.varRef()), buildRecvArgs(x.recvArgs()), false);
        }).other(x -> {
            throw new Unreachable();
        });

        return result.v;
    }

    private List<List<AstStatement>> buildChoices(ChoicesContext ctx) {
        return ctx.sequence().stream()
                .map(this::buildStatementFromSequence)
                .collect(Collectors.toCollection(NonNullArrayList::new));
    }

    private List<AstDeclare> buildDeclareList(DeclareListContext ctx) {
        List<AstDeclare> result = new NonNullArrayList<>();

        for (OneDeclareContext oneDeclare : ctx.oneDeclare()) {
            result.addAll(buildOneDeclareAll(oneDeclare));
        }

        return result;
    }

    private List<AstExpr> buildExprList(List<? extends ExprContext> rawExprs) {
        return rawExprs.stream()
                .map(x -> (AstExpr) visit(x))
                .collect(Collectors.toCollection(NonNullArrayList::new));
    }

    private AstProctype buildInit(InitContext ctx) {
        Optional<Integer> priority = Optional.empty();
        if (ctx.priority() != null) {
            priority = Optional.of(ConstExprCalculator.calc(ctx.priority().constExpr()));
        }

        AstStatementsBlock statementBlock = buildStatementsBlock(ctx.statementBlock());

        return new AstProctype(true, Optional.empty(), "init", new NonNullArrayList<>(),
                priority, statementBlock.statements);
    }

    private AstInline buildInline(InlineContext ctx) {
        String name = ctx.name.getText();
        List<String> args = ctx.args.stream().map(Token::getText)
                .collect(Collectors.toCollection(NonNullArrayList::new));
        List<AstStatement> statements = buildStatementFromSequence(ctx.statementBlock().sequence());

        return new AstInline(name, args, statements);
    }

    private AstMtype buildMtype(MtypeContext ctx) {
        Optional<String> subType = Optional.empty();
        if (ctx.subType != null) {
            subType = Optional.of(ctx.subType.getText());
        }

        List<String> mtypeName;
        if (ctx.mtypeName != null) {
            mtypeName = ctx.mtypeName.stream()
                    .map(Token::getText)
                    .collect(Collectors.toCollection(NonNullArrayList::new));
        } else {
            mtypeName = new NonNullArrayList<>();
        }

        return new AstMtype(mtypeName, subType);
    }

    private List<AstDeclare> buildOneDeclareAll(OneDeclareContext oneDeclare) {
        List<AstDeclare> result = new NonNullArrayList<>();

        SubclassVisitor.visit(
                oneDeclare
        ).when(OneDeclare_NormalContext.class, x -> {
            result.addAll(buildOneDeclareOfNormal(x));
        }).when(OneDeclare_UnsignedContext.class, x -> {
            result.addAll(buildOneDeclareOfUnsigned(x));
        }).other(x -> {
            throw new Unreachable();
        });

        return result;
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

        List<AstDeclare> result = new NonNullArrayList<>();
        for (InitVarContext initVar : ctx.initVar()) {
            var info = extractInitVar(initVar);
            result.add(new AstNormalDeclare(show, hidden, local, typeName, info.a, info.b, info.c));
        }

        return result;
    }

    private List<AstDeclare> buildOneDeclareOfUnsigned(OneDeclare_UnsignedContext ctx) {
        List<AstDeclare> result = new NonNullArrayList<>();

        for (var unsignItem : ctx.unsignedItem()) {
            String name = unsignItem.IDENTIFIER().getText();
            int bitLen = Integer.parseInt(unsignItem.NUMBER().getText());

            Optional<AstVarInit> init = Optional.empty();
            if (unsignItem.anyExpr() != null) {
                init = Optional.of(new AstExprAsInit((AstExpr) visit(unsignItem.anyExpr())));
            }

            result.add(new AstUnsignedDeclare(name, bitLen, init));
        }

        return result;
    }

    private AstProctype buildProctype(ProctypeContext ctx) {
        boolean active = false;
        if (ctx.ACTIVE() != null) {
            active = true;
        }

        String name = ctx.IDENTIFIER().getText();

        List<AstDeclare> parameters;
        if (ctx.declareList() != null) {
            parameters = buildDeclareList(ctx.declareList());
        } else {
            parameters = new NonNullArrayList<>();
        }

        Optional<Integer> priority = Optional.empty();
        if (ctx.priority() != null) {
            priority = Optional.of(ConstExprCalculator.calc(ctx.priority().constExpr()));
        }

        Optional<AstExpr> enabler = Optional.empty();
        if (ctx.enabler() != null) {
            enabler = Optional.of((AstExpr) visit(ctx.enabler().expr()));
        }

        AstStatementsBlock statementBlock = buildStatementsBlock(ctx.statementBlock());

        return new AstProctype(active, enabler, name, parameters, priority, statementBlock.statements);
    }

    private List<AstRecvArgItem> buildRecvArgs(RecvArgsContext recvArgs) {
        List<AstRecvArgItem> result = new NonNullArrayList<>();
        extractRecvArgsRecursive(recvArgs, result);
        return result;
    }

    private List<AstExpr> buildSendArgs(SendArgsContext sendArgs) {
        List<AstExpr> result = new NonNullArrayList<>();

        SubclassVisitor.visit(
                sendArgs
        ).when(SendArgs_NormalContext.class, x -> {
            result.addAll(buildAnyExprList(x.argList().anyExpr()));
        }).when(SendArgs_WithTypeContext.class, x -> {
            result.add((AstExpr) visit(x.anyExpr()));
            result.addAll(buildAnyExprList(x.argList().anyExpr()));
        }).other(x -> {
            throw new Unreachable();
        });

        return result;
    }

    private AstSendStatement buildSendStatement(SendContext ctx) {
        Cell<AstSendStatement> result = Cells.empty();

        SubclassVisitor.visit(
                ctx
        ).when(Send_FifoContext.class, x -> {
            result.v = new AstSendStatement(
                    buildVarRef(x.varRef()), buildSendArgs(x.sendArgs()), true);
        }).when(Send_InsertContext.class, x -> {
            result.v = new AstSendStatement(
                    buildVarRef(x.varRef()), buildSendArgs(x.sendArgs()), false);
        }).other(x -> {
            throw new Unreachable();
        });

        return result.v;
    }

    private List<AstStatement> buildStatementFromSequence(SequenceContext sequence) {
        return sequence.step().stream()
                .map(this::buildStep)
                .collect(Collectors.toCollection(NonNullArrayList::new));
    }

    private AstStatementsBlock buildStatementsBlock(StatementBlockContext ctx) {
        return new AstStatementsBlock(buildStatementFromSequence(ctx.sequence()));
    }

    private AstStatement buildStep(StepContext step) {
        Cell<AstStatement> result = Cells.empty();

        SubclassVisitor.visit(
                step
        ).when(Step_NormalStatementContext.class, x -> {
            result.v = (AstStatement) visit(x.statement());
        }).when(Step_UnlessStatementContext.class, x -> {
            AstStatement doThis = (AstStatement) visit(x.doThis),
                    ifThisBlocking = (AstStatement) visit(x.ifThisBlocking);

            result.v = new AstUnlessStatement(doThis, ifThisBlocking);
        }).other(x -> {
            throw new Unreachable();
        });

        return result.v;
    }

    private AstUtype buildUtype(UtypeContext ctx) {
        String name = ctx.IDENTIFIER().getText();
        List<AstDeclare> declares = buildDeclareList(ctx.declareList());

        return new AstUtype(name, declares);
    }

    private AstVarRef buildVarRef(VarRefContext ctx) {
        AstVarRef result = new AstVarNameAccess(ctx.IDENTIFIER().getText());
        if (ctx.anyExpr() != null) {
            AstExpr expr = (AstExpr) visit(ctx.anyExpr());
            result = new AstArrayAccess(result, expr);
        }

        if (ctx.varRef() != null) {
            result = recursiveBuildVarRef(ctx.varRef(), result);
        }

        return result;
    }

    /**
     * 从 InitVarContext 中提取信息。
     *
     * @param ctx InitVarContext 实例
     * @return (数组长度, 变量名, 初值)
     */
    private Tuple3<Integer, String, Optional<AstVarInit>> extractInitVar(InitVarContext ctx) {
        Cell<Integer> arrayLen = Cells.empty();
        Cell<Optional<AstVarInit>> initVar = Cells.empty();

        SubclassVisitor.visit(
                ctx
        ).when(InitVar_ArrayInitializerListContext.class, x -> {
            arrayLen.v = ConstExprCalculator.calc(x.constExpr());
            initVar.v = Optional.of(new AstInitializerList(buildAnyExprList(x.initializerList().anyExpr())));

        }).when(InitVar_ArrayChanInitContext.class, x -> {
            arrayLen.v = ConstExprCalculator.calc(x.constExpr());
            initVar.v = Optional.of(buildChanInit(x.chanInit()));

        }).when(InitVar_ArrayAnyExprContext.class, x -> {
            arrayLen.v = ConstExprCalculator.calc(x.constExpr());
            initVar.v = Optional.of(new AstExprAsInit((AstExpr) visit(x.anyExpr())));

        }).when(InitVar_ArrayContext.class, x -> {
            arrayLen.v = ConstExprCalculator.calc(x.constExpr());
            initVar.v = Optional.empty();

        }).when(InitVar_AnyExprContext.class, x -> {
            arrayLen.v = -1;
            initVar.v = Optional.of(new AstExprAsInit((AstExpr) visit(x.anyExpr())));

        }).when(InitVar_ChanInitContext.class, x -> {
            arrayLen.v = -1;
            initVar.v = Optional.of(buildChanInit(x.chanInit()));

        }).when(InitVar_NoInitContext.class, x -> {
            arrayLen.v = -1;
            initVar.v = Optional.empty();

        }).other(x -> {
            throw new Unreachable();
        });

        return Tuple3.of(arrayLen.v, ctx.varName, initVar.v);
    }

    private void extractRecvArgsRecursive(RecvArgsContext recvArgs, List<AstRecvArgItem> result) {
        SubclassVisitor.visit(
                recvArgs
        ).when(RecvArgs_NormalContext.class, x -> {
            for (var recvArgItem : x.recvArgItem()) {
                result.add((AstRecvArgItem) visit(recvArgItem));
            }
        }).when(RecvArgs_WithBracketContext.class, x -> {
            result.add((AstRecvArgItem) visit(x.recvArgItem()));
            extractRecvArgsRecursive(recvArgs, result);
        }).other(x -> {
            throw new Unreachable();
        });
    }

    private AstVarRef recursiveBuildVarRef(VarRefContext ctx, AstVarRef oldAst) {
        AstVarRef result = new AstMemberAccess(oldAst, ctx.IDENTIFIER().getText());
        if (ctx.anyExpr() != null) {
            AstExpr expr = (AstExpr) visit(ctx.anyExpr());
            result = new AstArrayAccess(result, expr);
        }

        if (ctx.varRef() != null) {
            result = recursiveBuildVarRef(ctx.varRef(), result);
        }

        return result;
    }

    @Override
    public AstNode visitChildren(RuleNode node) {
        throw new Error("visitChildren 被触发");
    }

    @Override
    public AstRecvStatement visitReceive_Fifo(Receive_FifoContext ctx) {
        return new AstRecvStatement(buildVarRef(ctx.varRef()), buildRecvArgs(ctx.recvArgs()),
                false, true);
    }

    @Override
    public AstRecvStatement visitReceive_Random(Receive_RandomContext ctx) {
        return new AstRecvStatement(buildVarRef(ctx.varRef()), buildRecvArgs(ctx.recvArgs()),
                false, false);
    }

    @Override
    public AstRecvStatement visitReceive_PollFifo(Receive_PollFifoContext ctx) {
        return new AstRecvStatement(buildVarRef(ctx.varRef()), buildRecvArgs(ctx.recvArgs()),
                true, true);
    }

    @Override
    public AstRecvStatement visitReceive_PollRandom(Receive_PollRandomContext ctx) {
        return new AstRecvStatement(buildVarRef(ctx.varRef()), buildRecvArgs(ctx.recvArgs()),
                true, false);
    }

    @Override
    public AstWriteOnlyRecvArg visitRecvArgItem_WriteOnly(RecvArgItem_WriteOnlyContext ctx) {
        return new AstWriteOnlyRecvArg();
    }

    @Override
    public AstVarRefRecvArg visitRecvArgItem_VarRef(RecvArgItem_VarRefContext ctx) {
        return new AstVarRefRecvArg(buildVarRef(ctx.varRef()));
    }

    @Override
    public AstEvalExprRecvArg visitRecvArgItem_Eval(RecvArgItem_EvalContext ctx) {
        return new AstEvalExprRecvArg((AstExpr) visit(ctx.anyExpr()));
    }

    @Override
    public AstConstRecvArg visitRecvArgItem_Constant(RecvArgItem_ConstantContext ctx) {
        return new AstConstRecvArg(ConstExprCalculator.calc(ctx.constExpr()));
    }

    @Override
    public AstDeclareStatement visitStatement_OneDeclare(Statement_OneDeclareContext ctx) {
        return new AstDeclareStatement(
                buildOneDeclareAll(ctx.oneDeclare()));
    }

    @Override
    public AstStatement visitStatement_Xr(Statement_XrContext ctx) {
        return new AstStatement() {
            @Override
            public boolean isDummy() {
                return true;
            }
        };
    }

    @Override
    public AstStatement visitStatement_Xs(Statement_XsContext ctx) {
        return new AstStatement() {
            @Override
            public boolean isDummy() {
                return true;
            }
        };
    }

    @Override
    public AstIfDoStatement visitStatement_If(Statement_IfContext ctx) {
        return new AstIfDoStatement(buildChoices(ctx.choices()), false);
    }

    @Override
    public AstIfDoStatement visitStatement_Do(Statement_DoContext ctx) {
        return new AstIfDoStatement(buildChoices(ctx.choices()), true);
    }

    @Override
    public AstStatement visitStatement_For(Statement_ForContext ctx) {
        List<AstStatement> statements = buildStatementFromSequence(
                ctx.statementBlock().sequence());
        Cell<AstStatement> result = Cells.empty();

        SubclassVisitor.visit(
                ctx.range()
        ).when(Range_NumericContext.class, x -> {
            result.v = new AstForStatement(
                    buildVarRef(x.varRef()),
                    (AstExpr) visit(x.lower), (AstExpr) visit(x.upper), statements);
        }).when(Range_IterateContext.class, x -> {
            result.v = new AstForInStatement(
                    buildVarRef(x.storeTo), buildVarRef(x.iterateFrom), statements);
        }).other(x -> {
            throw new Unreachable();
        });

        return result.v;
    }

    @Override
    public AstAtomicStatement visitStatement_Atomic(Statement_AtomicContext ctx) {
        return new AstAtomicStatement(buildStatementFromSequence(ctx.statementBlock().sequence()), false);
    }

    @Override
    public AstAtomicStatement visitStatement_Dstep(Statement_DstepContext ctx) {
        return new AstAtomicStatement(buildStatementFromSequence(ctx.statementBlock().sequence()), true);
    }

    @Override
    public AstSelectStatement visitStatement_Select(Statement_SelectContext ctx) {
        return new AstSelectStatement(buildVarRef(ctx.varRef()),
                (AstExpr) visit(ctx.lower), (AstExpr) visit(ctx.upper));
    }

    @Override
    public AstStatementsBlock visitStatement_Compound(Statement_CompoundContext ctx) {
        return buildStatementsBlock(ctx.statementBlock());
    }

    @Override
    public AstBreakStatement visitStatement_Break(Statement_BreakContext ctx) {
        return new AstBreakStatement();
    }

    @Override
    public AstGotoStatement visitStatement_Goto(Statement_GotoContext ctx) {
        return new AstGotoStatement(ctx.IDENTIFIER().getText());
    }

    @Override
    public AstLabeledStatement visitStatement_Labeled(Statement_LabeledContext ctx) {
        return new AstLabeledStatement(ctx.IDENTIFIER().getText(), (AstStatement) visit(ctx.statement()));
    }

    @Override
    public AstPrintfStatement visitStatement_Printf(Statement_PrintfContext ctx) {
        String printTemplateRaw = ctx.STRING().getText();
        String printTemplate = StringEscapeUtils.unescapeJava(
                printTemplateRaw.substring(1, printTemplateRaw.length() - 1));

        List<AstExpr> exprList;
        if (ctx.argList() == null) {
            exprList = new NonNullArrayList<>();
        } else {
            exprList = buildAnyExprList(ctx.argList().anyExpr());
        }

        int templateParamNum = PromelaPrintfUtil.parseTemplate(printTemplate);
        if (exprList.size() != templateParamNum) {
            throw new SyntaxException("printf 模板字符串中，% 参数的个数与传入的参数个数不匹配");
        }

        return new AstPrintfStatement(printTemplate, exprList);
    }

    @Override
    public AstPrintfStatement visitStatement_Printm(Statement_PrintmContext ctx) {
        var exprs = new NonNullArrayList<AstExpr>();
        exprs.add(new AstVarRefExpr(buildVarRef(ctx.varRef())));

        return new AstPrintfStatement("%e", exprs);
    }

    @Override
    public AstAssertStatement visitStatement_Assert(Statement_AssertContext ctx) {
        return new AstAssertStatement((AstExpr) visit(ctx.expr()));
    }

    @Override
    public AstBlockableStatement visitStatement_Expr(Statement_ExprContext ctx) {
        return new AstBlockableStatement((AstExpr) visit(ctx.expr()));
    }

    @Override
    public AstSendStatement visitStatement_Send(Statement_SendContext ctx) {
        return buildSendStatement(ctx.send());
    }

    @Override
    public AstRecvStatement visitStatement_Receive(Statement_ReceiveContext ctx) {
        return (AstRecvStatement) visit(ctx.receive());
    }

    @Override
    public AstAssignment visitStatement_Assign(Statement_AssignContext ctx) {
        AssignmentContext assign = ctx.assignment();
        Cell<AstAssignment> result = Cells.empty();

        SubclassVisitor.visit(
                assign
        ).when(Assignment_DummyContext.class, x -> {
        }).when(Assignment_IncreaseContext.class, x -> {
            result.v = new AstAdditionStatement(buildVarRef(x.varRef()), 1);
        }).when(Assignment_DecreaseContext.class, x -> {
            result.v = new AstAdditionStatement(buildVarRef(x.varRef()), -1);
        }).when(Assignment_NormalContext.class, x -> {
            result.v = new AstSetValueStatement(buildVarRef(x.varRef()), (AstExpr) visit(x.anyExpr()));
        }).other(x -> {
            throw new Unreachable();
        });

        return result.v;
    }

    @Override
    public AstCallInlineStatement visitStatement_CallInline(Statement_CallInlineContext ctx) {
        String name = ctx.IDENTIFIER().getText();
        List<AstExpr> args = ctx.expr().stream()
                .map(x -> (AstExpr) visit(x))
                .collect(Collectors.toCollection(NonNullArrayList::new));

        return new AstCallInlineStatement(name, args);
    }

    @Override
    public AstTernaryExpr visitAnyExpr_Ternary(AnyExpr_TernaryContext ctx) {
        return new AstTernaryExpr(
                (AstExpr) visit(ctx.cond),
                (AstExpr) visit(ctx.ifTrue),
                (AstExpr) visit(ctx.ifFalse));
    }

    @Override
    public AstConstExpr visitAnyExpr_Constant(AnyExpr_ConstantContext ctx) {
        return new AstConstExpr(ConstExprCalculator.calc(ctx.constExpr()));
    }

    @Override
    public AstPredefVarExpr visitAnyExpr_PredefVar(AnyExpr_PredefVarContext ctx) {
        String predefVarText = ctx.getChild(TerminalNode.class, 0).getText();
        if (PredefVar.unsupported.contains(predefVarText)) {
            throw new NotSupportedException(
                    "代码中使用的 " + predefVarText + " 变量仅能在 never/trace/ltl 块中使用，目前不支持这些语句块");
        }

        PredefVar v = PredefVar.from.get(Objects.requireNonNull(predefVarText));
        return new AstPredefVarExpr(v);
    }

    @Override
    public AstExpr visitAnyExpr_Enabled(AnyExpr_EnabledContext ctx) {
        throw new NotSupportedException(
                "代码中的 enabled() 语句仅能在 never/trace/ltl 块中使用，目前不支持这些语句块");
    }

    @Override
    public AstExpr visitAnyExpr_RemoteRef(AnyExpr_RemoteRefContext ctx) {
        throw new NotSupportedException("目前暂不支持 remote ref 语法");
    }

    @Override
    public AstExpr visitAnyExpr_Compound(AnyExpr_CompoundContext ctx) {
        return (AstExpr) visit(ctx.anyExpr());
    }

    @Override
    public AstExpr visitAnyExpr_PcValue(AnyExpr_PcValueContext ctx) {
        throw new NotSupportedException("Jamelin 的状态图实现与 SPIN 不同，因此不支持 pc_value");
    }

    @Override
    public AstLenExpr visitAnyExpr_Len(AnyExpr_LenContext ctx) {
        return new AstLenExpr(buildVarRef(ctx.varRef()));
    }

    @Override
    public AstExpr visitAnyExpr_Unary(AnyExpr_UnaryContext ctx) {
        return new AstUnaryExpr(ctx.getChild(TerminalNode.class, 0).getText(),
                (AstExpr) visit(ctx.anyExpr()));
    }

    @Override
    public AstExpr visitAnyExpr_SetPriority(AnyExpr_SetPriorityContext ctx) {
        throw new NotSupportedException("暂不支持与 proctype 的优先级有关的特性");
    }

    @Override
    public AstChanPollExpr visitAnyExpr_Poll(AnyExpr_PollContext ctx) {
        return buildChanPollExpr(ctx.poll());
    }

    @Override
    public AstRunExpr visitAnyExpr_Run(AnyExpr_RunContext ctx) {
        List<AstExpr> args;
        if (ctx.argList() == null) {
            args = new NonNullArrayList<>();
        } else {
            args = buildAnyExprList(ctx.argList().anyExpr());
        }
        Optional<Integer> priority = Optional.empty();
        if (ctx.priority() != null) {
            priority = Optional.of(ConstExprCalculator.calc(ctx.priority().constExpr()));
        }

        return new AstRunExpr(ctx.IDENTIFIER().getText(), args, priority);
    }

    @Override
    public AstExpr visitAnyExpr_GetPriority(AnyExpr_GetPriorityContext ctx) {
        throw new NotSupportedException("暂不支持与 proctype 的优先级有关的特性");
    }

    @Override
    public AstVarRefExpr visitAnyExpr_VarRef(AnyExpr_VarRefContext ctx) {
        return new AstVarRefExpr(buildVarRef(ctx.varRef()));
    }

    @Override
    public AstExpr visitAnyExpr_Binary(AnyExpr_BinaryContext ctx) {
        return new AstBinaryExpr(ctx.getChild(TerminalNode.class, 0).getText(),
                (AstExpr) visit(ctx.anyExpr().get(0)), (AstExpr) visit(ctx.anyExpr().get(1)));
    }

    @Override
    public AstExpr visitExpr_Binary(Expr_BinaryContext ctx) {
        return new AstBinaryExpr(ctx.getChild(TerminalNode.class, 0).getText(),
                (AstExpr) visit(ctx.expr().get(0)), (AstExpr) visit(ctx.expr().get(1)));
    }

    @Override
    public AstNode visitExpr_ChanPoll(Expr_ChanPollContext ctx) {
        return super.visitExpr_ChanPoll(ctx);
    }

    @Override
    public AstExpr visitExpr_Compound(Expr_CompoundContext ctx) {
        return (AstExpr) visit(ctx.expr());
    }

    @Override
    public AstExpr visitExpr_AnyExpr(Expr_AnyExprContext ctx) {
        return (AstExpr) visit(ctx.anyExpr());
    }
}
