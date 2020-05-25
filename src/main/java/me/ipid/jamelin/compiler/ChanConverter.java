package me.ipid.jamelin.compiler;

import me.ipid.jamelin.ast.Ast.*;
import me.ipid.jamelin.constant.PromelaLanguage.ChanStatusOp;
import me.ipid.jamelin.entity.CompileTimeInfo;
import me.ipid.jamelin.entity.il.*;
import me.ipid.jamelin.entity.sa.SAPrimitiveType;
import me.ipid.jamelin.entity.sa.SATypeFactory;
import me.ipid.jamelin.entity.sa.SATypeFactory.PrimitiveTypesLib;
import me.ipid.jamelin.entity.sa.SATypedExpr;
import me.ipid.jamelin.entity.sa.SATypedMemLoc;
import me.ipid.jamelin.exception.CompileExceptions.NotSupportedException;
import me.ipid.jamelin.exception.CompileExceptions.SyntaxException;
import me.ipid.jamelin.util.Slot;
import me.ipid.util.errors.Unreachable;
import me.ipid.util.tupling.Tuple2;

import java.util.ArrayList;
import java.util.List;

public class ChanConverter {
    public static ILChanLenExpr buildLenExpr(
            CompileTimeInfo cInfo, AstLenExpr x
    ) {
        ILExpr chanId = buildChanId(cInfo, x.target);
        return new ILChanLenExpr(chanId);
    }

    public static ILChanPollExpr buildPollExpr(
            CompileTimeInfo cInfo, AstChanPollExpr x
    ) {
        ILExpr chanId = buildChanId(cInfo, x.chan);

        List<Integer> typeIds = new ArrayList<>();
        List<ILRecvArgItem> recvArgs = new ArrayList<>();
        buildRecvArgs(cInfo, x.args, typeIds, recvArgs);

        return new ILChanPollExpr(recvArgs, chanId, typeIds);
    }

    /**
     * 构建接收语句（及其对应的条件）
     *
     * @return (接收语句, 表示 「 是否可接收 」 的条件表达式)
     */
    public static Tuple2<ILRecvStatement, ILChanPollExpr> buildRecvStatement(
            CompileTimeInfo cInfo, AstRecvStatement x
    ) {
        if (!x.fifo) {
            throw new NotSupportedException("暂不支持非 FIFO 的信道接收操作");
        }

        // 获取 chanId 表达式
        ILExpr chanId = buildChanId(cInfo, x.chan);
        List<Integer> typeIds = new ArrayList<>();
        List<ILRecvArgItem> recvArgs = new ArrayList<>();
        buildRecvArgs(cInfo, x.args, typeIds, recvArgs);

        return Tuple2.of(
                new ILRecvStatement(recvArgs, chanId, typeIds, x.peek),
                new ILChanPollExpr(recvArgs, chanId, typeIds)
        );
    }

    public static Tuple2<ILSendStatement, ILChanStatusExpr> buildSendStatement(
            CompileTimeInfo cInfo, AstSendStatement x
    ) {
        if (!x.fifo) {
            throw new NotSupportedException("暂不支持非 FIFO 的信道发送操作");
        }

        ILExpr chanId = buildChanId(cInfo, x.chan);

        List<ILSendArgItem> sendArgs = new ArrayList<>();
        List<Integer> typeIds = new ArrayList<>();

        // 发送、接收语句是唯一允许非原始类型表达式的地方
        for (var astExpr : x.args) {
            // 由于非原始类型 VarRef 不能直接构造成表达式，因此必须特殊处理
            if (astExpr instanceof AstVarRefExpr) {
                // 假设表达式是变量的引用
                SATypedMemLoc typedSlot = VarRefConverter.buildTypedMemLocOfVarRef(
                        cInfo, ((AstVarRefExpr) astExpr).vRef);

                // 提取 type id 和 send arg
                typeIds.add(typedSlot.type.getTypeId());
                sendArgs.add(new ILRangeSendArg(VarRefConverter.buildRangeFromTypedMemLoc(typedSlot)));
            } else {
                // 假设表达式是运算的表达式
                SATypedExpr typedExpr = ExprConverter.buildExpr(cInfo, astExpr);

                // 运算会导致整数提升，因此类型必然是 int
                assert typedExpr.type.equals(PrimitiveTypesLib.int_t);

                // 提取 type id 和 send arg
                typeIds.add(PrimitiveTypesLib.int_t.getTypeId());
                sendArgs.add(new ILExprSendArg(typedExpr.expr));
            }
        }

        return Tuple2.of(
                new ILSendStatement(chanId, sendArgs, typeIds),
                new ILChanStatusExpr(chanId, ChanStatusOp.NFULL)
        );
    }

    public static ILChanStatusExpr buildStatusExpr(
            CompileTimeInfo cInfo, AstChanStatusExpr x
    ) {
        ILExpr chanId = buildChanId(cInfo, x.chan);
        return new ILChanStatusExpr(chanId, x.op);
    }

    private static ILExpr buildChanId(CompileTimeInfo cInfo, AstVarRef astChanId) {
        SATypedMemLoc chanIdSlot = VarRefConverter.buildTypedMemLocOfVarRef(cInfo, astChanId);
        if (!(chanIdSlot.type instanceof SAPrimitiveType)) {
            throw new SyntaxException("类型 " + chanIdSlot.type.getName() + " 不是原始类型，不能把它当作信道");
        }

        return chanIdSlot.buildGetExpr();
    }

    private static void buildRecvArgs(
            CompileTimeInfo cInfo, List<AstRecvArgItem> astArgs, List<Integer> typeIds, List<ILRecvArgItem> ilArgs
    ) {
        // 遍历每一个 AstRecvArgItem，根据其类型分发到子函数中处理
        for (AstRecvArgItem astArg : astArgs) {
            if (astArg instanceof AstVarRefRecvArg) {
                handleAstVarRefRecvArg(cInfo, (AstVarRefRecvArg) astArg, typeIds, ilArgs);
            } else if (astArg instanceof AstEvalExprRecvArg) {
                handleAstEvalRecvArg(cInfo, (AstEvalExprRecvArg) astArg, typeIds, ilArgs);
            } else if (astArg instanceof AstConstRecvArg) {
                typeIds.add(PrimitiveTypesLib.int_t.getTypeId());
                ilArgs.add(new ILConstRecvArg(((AstConstRecvArg) astArg).num));
            } else if (astArg instanceof AstWriteOnlyRecvArg) {
                // 此处用「不可能的 type id」表示「任意类型」
                typeIds.add(SATypeFactory.MAX_TYPE_ID + 1);
                ilArgs.add(ILWriteOnlyRecvArg.INSTANCE);
            } else {
                throw new Unreachable();
            }
        }
    }

    private static void handleAstEvalRecvArg(
            CompileTimeInfo cInfo, AstEvalExprRecvArg astArg, List<Integer> typeIds, List<ILRecvArgItem> recvArgs
    ) {
        // 接收语句的 eval 允许使用非原始类型，因此需要特殊处理
        if (astArg.expr instanceof AstVarRefExpr) {
            AstVarRef vRef = ((AstVarRefExpr) astArg.expr).vRef;
            SATypedMemLoc targetSlot = VarRefConverter.buildTypedMemLocOfVarRef(cInfo, vRef);

            typeIds.add(targetSlot.type.getTypeId());
            recvArgs.add(new ILEvalRangeRecvArg(
                    targetSlot.type.getSize(), VarRefConverter.buildRangeFromTypedMemLoc(targetSlot)
            ));
        } else {
            SATypedExpr typedExpr = ExprConverter.buildExpr(cInfo, astArg.expr);
            assert typedExpr.type.equals(PrimitiveTypesLib.int_t);

            typeIds.add(typedExpr.type.getTypeId());
            recvArgs.add(new ILEvalExprRecvArg(typedExpr.expr));
        }
    }

    private static void handleAstVarRefRecvArg(
            CompileTimeInfo cInfo, AstVarRefRecvArg arg,
            List<Integer> typeIds, List<ILRecvArgItem> recvArgs
    ) {
        // 如果是形如 x.a.b 的 VarRef 接收参数

        SATypedMemLoc targetSlot = VarRefConverter.buildTypedMemLocOfVarRef(cInfo,
                arg.target);

        if (targetSlot.type instanceof SAPrimitiveType) {
            // 如果接收的目标变量类型是原始类型，则加入数字舍入的逻辑

            var targetType = (SAPrimitiveType) targetSlot.type;
            recvArgs.add(new ILPrimitiveRecvArg(
                    new Slot(targetType.bitLen, targetType.signed),
                    targetSlot.combineOffset(),
                    targetSlot.global
            ));

            // 将目标变量当作 int 类型的变量（以方便 typeid 一致性检查）
            typeIds.add(PrimitiveTypesLib.int_t.getTypeId());
        } else {
            // 如果接收的目标变量类型不是原始类型，那就直接 memcpy（指使用 ILRange）
            ILRange range = VarRefConverter.buildRangeFromTypedMemLoc(targetSlot);

            // 收集 type id 和 recv arg
            recvArgs.add(new ILRangeRecvArg(targetSlot.type.getSize(), range));
            typeIds.add(targetSlot.type.getTypeId());
        }
    }
}
