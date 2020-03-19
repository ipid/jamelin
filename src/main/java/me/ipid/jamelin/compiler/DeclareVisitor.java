package me.ipid.jamelin.compiler;

import com.google.common.collect.ImmutableMap;
import me.ipid.jamelin.entity.*;
import me.ipid.jamelin.entity.expr.*;
import me.ipid.jamelin.entity.statement.*;
import me.ipid.jamelin.entity.symbol.*;
import me.ipid.jamelin.exception.*;
import me.ipid.jamelin.thirdparty.antlr.*;
import me.ipid.jamelin.thirdparty.antlr.PromelaAntlrParser.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * 负责访问 declareList 及其子树。
 */
public class DeclareVisitor
        extends PromelaAntlrBaseVisitor<List<PromelaStatement>> {

    private static final Logger logger = LogManager.getLogger(DeclareVisitor.class);
    private static final ConstExprVisitor constExprVisitor = new ConstExprVisitor();
    private static final Map<Integer, Integer> lengthMap =
            new ImmutableMap.Builder<Integer, Integer>()
                    .put(PromelaAntlrLexer.BIT, 1)
                    .put(PromelaAntlrLexer.BOOL, 1)
                    .put(PromelaAntlrLexer.BYTE, 8)
                    .put(PromelaAntlrLexer.CHAN, 8)
                    .put(PromelaAntlrLexer.SHORT, 16)
                    .put(PromelaAntlrLexer.INT, 32)
                    .put(PromelaAntlrLexer.MTYPE, 8)
                    .put(PromelaAntlrLexer.PID, 8)
                    .build();
    private CompileTimeInfo info;
    private CurrDeclare currDecl;
    private List<PromelaStatement> assignments;

    public DeclareVisitor(CompileTimeInfo info) {
        this.info = info;
        this.currDecl = new CurrDeclare();
        this.assignments = new ArrayList<>();
    }

    @Override
    public List<PromelaStatement> visitOneDeclare_Normal(OneDeclare_NormalContext ctx) {
        for (ParseTree tree : ctx.children) {
            if (tree instanceof TerminalNode) {
                // 如果是终结符，则获取 token 类型
                TerminalNode token = (TerminalNode) tree;
                int tokenType = token.getSymbol().getType();

                if (tokenType == PromelaAntlrLexer.PUNC_COMMA) {
                    // 如果当前符号是逗号
                    putIntoTable();
                } else if (tokenType == PromelaAntlrLexer.IDENTIFIER) {
                    // 如果现在在定义变量名
                    currDecl.varName = token.getText();
                }

            } else {
                // 如果不是终结符，就继续遍历
                visit(tree);
            }

        }
        // 最后一个声明是没有逗号的
        putIntoTable();

        return clearAssignments();
    }

    @Override
    public List<PromelaStatement> visitOneDeclare_Unsigned(OneDeclare_UnsignedContext ctx) {
        throw new NotSupportedException("目前暂不支持 unsigned 变量");
    }

    /**
     * 获取有无符号、位长度
     */
    @Override
    public List<PromelaStatement> visitTypeName(PromelaAntlrParser.TypeNameContext ctx) {
        // 获取类型信息
        TerminalNode typeNode = (TerminalNode) ctx.getChild(0);
        int typeNum = typeNode.getSymbol().getType();

        if (typeNum == PromelaAntlrLexer.IDENTIFIER) {
            throw new NotSupportedException("目前暂不支持自定义 struct");
        }
        currDecl.userType = Optional.empty();

        // 获取位长度
        currDecl.bitLen = lengthMap.get(typeNum);

        // 获取有无符号
        currDecl.signed = typeNum == PromelaAntlrLexer.INT || typeNum == PromelaAntlrLexer.SHORT;

        return null;
    }

    @Override
    public List<PromelaStatement> visitInitVar_ArrayInitializerList(InitVar_ArrayInitializerListContext ctx) {
        currDecl.arrayLen = constExprVisitor.visit(ctx.constExpr());
        return null;
    }

    @Override
    public List<PromelaStatement> visitInitVar_ArrayChanInit(InitVar_ArrayChanInitContext ctx) {
        currDecl.arrayLen = constExprVisitor.visit(ctx.constExpr());
        return null;
    }

    @Override
    public List<PromelaStatement> visitInitVar_ArrayAnyExpr(InitVar_ArrayAnyExprContext ctx) {
        currDecl.arrayLen = constExprVisitor.visit(ctx.constExpr());
        return null;
    }

    @Override
    public List<PromelaStatement> visitInitVar_Array(InitVar_ArrayContext ctx) {
        currDecl.arrayLen = constExprVisitor.visit(ctx.constExpr());
        return null;
    }

    @Override
    public List<PromelaStatement> visitInitVar_AnyExpr(InitVar_AnyExprContext ctx) {
        currDecl.arrayLen = -1;

        // 将计算初始值的表达式存进去
        ExprVisitor visitor = new ExprVisitor(info);
        currDecl.initialValue = Optional.of(visitor.visit(ctx.anyExpr()));

        return null;
    }

    @Override
    public List<PromelaStatement> visitInitVar_ChanInit(InitVar_ChanInitContext ctx) {
        currDecl.arrayLen = -1;
        return null;
    }

    /**
     * 将当前整理出的变量信息放到符号表里。
     */
    private void putIntoTable() {
        // 获取当前类型
        PromelaType varType;
        if (!currDecl.userType.isPresent()) {
            // 如果是简单类型
            varType = new SimplePromelaType(
                    currDecl.signed, currDecl.bitLen, currDecl.arrayLen);
        } else {
            // TODO: 如果是用户自定义类型
            throw new NotSupportedException("目前暂不支持自定义类型");
        }
        info.getTable().putVar(currDecl.varName, varType);

        // 如果之前 visit initVar 的时候搞出了初始值表达式
        if (currDecl.initialValue.isPresent()) {
            // 将赋值语句记录下来
            SymbolTableItem currItem = info.getTable().getVar(currDecl.varName).get();

            // TODO: 编写复杂类型的赋值逻辑
            PromelaStatement assignStatement = new SetMemoryStatement(
                    currItem.isGlobal(), currItem.getStartAddr(), currDecl.initialValue.get()
            );
            assignments.add(assignStatement);
        }

        currDecl.initialValue = Optional.empty();
    }

    private List<PromelaStatement> clearAssignments() {
        List<PromelaStatement> oldStatements = assignments;
        assignments = new ArrayList<>();
        return oldStatements;
    }

    public static final class CurrDeclare {
        public int arrayLen = -1, bitLen = 0;
        public boolean signed = false;

        public Optional<PromelaType> userType = Optional.empty();
        public String varName = null;

        public Optional<PromelaExpr> initialValue = Optional.empty();
    }
}
