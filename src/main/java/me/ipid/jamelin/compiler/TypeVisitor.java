package me.ipid.jamelin.compiler;

import com.google.common.collect.ImmutableMap;
import me.ipid.jamelin.constant.PromelaType;
import me.ipid.jamelin.entity.PromelaTypeInfo;
import me.ipid.jamelin.exception.NotSupportedException;
import me.ipid.jamelin.thirdparty.antlr.*;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.Map;
import java.util.Optional;

public class TypeVisitor extends PromelaAntlrBaseVisitor<PromelaTypeInfo> {

    private static final Map<Integer, Integer> lengthMap =
            new ImmutableMap.Builder<Integer, Integer>()
                    .put(PromelaAntlrLexer.BIT, 1)
                    .put(PromelaAntlrLexer.BOOL, 1)
                    .put(PromelaAntlrLexer.BYTE, 8)
                    .put(PromelaAntlrLexer.SHORT, 16)
                    .put(PromelaAntlrLexer.INT, 32)
                    .put(PromelaAntlrLexer.MTYPE, 8)
                    .put(PromelaAntlrLexer.CHAN, 8)
                    .build();

    @Override
    public PromelaTypeInfo visitTypeName(PromelaAntlrParser.TypeNameContext ctx) {
        TerminalNode typeNode = (TerminalNode) ctx.getChild(0);
        int typeNum = typeNode.getSymbol().getType();

        if (typeNum == PromelaAntlrLexer.IDENTIFIER) {
            throw new NotSupportedException("目前暂不支持自定义 struct");
        }

        PromelaType type;
        int bitLen;

        // 获取类型
        if (typeNum == PromelaAntlrLexer.CHAN) {
            type = PromelaType.CHAN;
        } else if (typeNum == PromelaAntlrLexer.BYTE || typeNum == PromelaAntlrLexer.MTYPE) {
            type = PromelaType.UNSIGNED;
        } else {
            type = PromelaType.SIGNED;
        }

        // 获取长度
        Optional<Integer> bitLenRaw = Optional.ofNullable(lengthMap.get(typeNum));
        if (!bitLenRaw.isPresent()) {
            throw new Error("类型不存在");
        }

        bitLen = bitLenRaw.get();

        // 暂不支持数组长度
        return new PromelaTypeInfo(
                type, bitLen, -1
        );
    }
}
