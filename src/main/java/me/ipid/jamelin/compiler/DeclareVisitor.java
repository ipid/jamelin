package me.ipid.jamelin.compiler;

import com.google.common.collect.ImmutableMap;
import me.ipid.jamelin.entity.code.PromelaExpr;
import me.ipid.jamelin.entity.symbol.*;
import me.ipid.jamelin.exception.NotSupportedException;
import me.ipid.jamelin.thirdparty.antlr.*;
import me.ipid.jamelin.thirdparty.antlr.PromelaAntlrParser.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * 负责访问 declareList 及其子树。
 */
public class DeclareVisitor extends PromelaAntlrBaseVisitor<Object> {

    private static final Logger logger = LogManager.getLogger(DeclareVisitor.class);

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

    private ScopeManager currScope;
    private Map<String, PromelaNamedItem> entities;

    private ConstExprVisitor constExprVisitor = new ConstExprVisitor();

    private int currArrayLen = -1, currBitLen = 0;
    private boolean isCurrSigned = false, isSimpleType = true;
    private PromelaType currUserType = null;
    private String currVarName = null;

    public DeclareVisitor(ScopeManager currScope, Map<String, PromelaNamedItem> entities) {
        this.currScope = currScope;
        this.entities = entities;
    }

    @Override
    public Object visitOneDeclare_Normal(OneDeclare_NormalContext ctx) {
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
                    currVarName = token.getText();
                }

            } else {
                // 如果不是终结符，就继续遍历
                visit(tree);
            }

        }
        // 最后一个声明是没有逗号的
        putIntoTable();

        return null;
    }

    /**
     * 将当前整理出的变量信息放到符号表里。
     */
    private void putIntoTable() {
        if (isSimpleType) {
            // 如果是简单类型
            SimplePromelaType type = new SimplePromelaType(isCurrSigned, currBitLen, currArrayLen);
            currScope.putVar(currVarName, type);

            logger.debug(String.format(
                    "创建变量 %s，%s符号，位长 %d，数组长 %d",
                    currVarName, (isCurrSigned ? "有" : "无"), currBitLen, currArrayLen
            ));
        } else {
            // TODO: 如果是用户自定义类型
            throw new NotSupportedException("目前暂不支持自定义类型");
        }
    }

    @Override
    public Object visitOneDeclare_Unsigned(OneDeclare_UnsignedContext ctx) {
        throw new NotSupportedException("目前暂不支持 unsigned 变量");
    }

    /**
     * 获取有无符号、位长度
     */
    @Override
    public Object visitTypeName(PromelaAntlrParser.TypeNameContext ctx) {
        // 获取类型信息
        TerminalNode typeNode = (TerminalNode) ctx.getChild(0);
        int typeNum = typeNode.getSymbol().getType();

        if (typeNum == PromelaAntlrLexer.IDENTIFIER) {
            throw new NotSupportedException("目前暂不支持自定义 struct");
        }

        isSimpleType = true;

        // 获取位长度
        currBitLen = lengthMap.get(typeNum);

        // 获取有无符号
        isCurrSigned = typeNum == PromelaAntlrLexer.INT || typeNum == PromelaAntlrLexer.SHORT;

        return null;
    }

    @Override
    public Object visitInitVar_ArrayInitializerList(InitVar_ArrayInitializerListContext ctx) {
        currArrayLen = constExprVisitor.visit(ctx.constExpr());
        return null;
    }

    @Override
    public Object visitInitVar_ArrayChanInit(InitVar_ArrayChanInitContext ctx) {
        currArrayLen = constExprVisitor.visit(ctx.constExpr());
        return null;
    }

    @Override
    public Object visitInitVar_ArrayAnyExpr(InitVar_ArrayAnyExprContext ctx) {
        currArrayLen = constExprVisitor.visit(ctx.constExpr());
        return null;
    }

    @Override
    public Object visitInitVar_Array(InitVar_ArrayContext ctx) {
        currArrayLen = constExprVisitor.visit(ctx.constExpr());
        return null;
    }

    @Override
    public Object visitInitVar_AnyExpr(InitVar_AnyExprContext ctx) {
        currArrayLen = -1;

        ExprVisitor visitor = new ExprVisitor(currScope, entities);
        visitor.visit(ctx.anyExpr());

        return null;
    }

    @Override
    public Object visitInitVar_ChanInit(InitVar_ChanInitContext ctx) {
        currArrayLen = -1;
        return null;
    }
}
