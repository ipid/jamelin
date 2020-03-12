package me.ipid.jamelin.compiler;

import me.ipid.jamelin.constant.PromelaType;
import me.ipid.jamelin.entity.PromelaTypeInfo;
import me.ipid.jamelin.thirdparty.antlr.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.Map;

public class TypeVisitor extends PromelaBaseVisitor<PromelaTypeInfo> {

    private Map<PromelaType, Integer> lengthMap;

    @Override
    public PromelaTypeInfo visitTypeName(PromelaParser.TypeNameContext ctx) {
        TerminalNode typeNode = (TerminalNode)ctx.getChild(0);
        int typeNum = typeNode.getSymbol().getType();

        PromelaType type;
        int bitLen;

        // 获取类型
        if (typeNum == PromelaLexer.CHAN) {
            type = PromelaType.CHAN;
        } else if (typeNum == PromelaLexer.BYTE || typeNum == PromelaLexer.MTYPE) {
            type = PromelaType.UNSIGNED;
        } else {
            type = PromelaType.SIGNED;
        }

        // 获取长度
        if (typeNum == PromelaLexer.BIT) {
            type = PromelaType.CHAN;
        } else if (typeNum == PromelaLexer.BYTE || typeNum == PromelaLexer.MTYPE) {
            type = PromelaType.UNSIGNED;
        } else {
            type = PromelaType.SIGNED;
        }
    }
}
