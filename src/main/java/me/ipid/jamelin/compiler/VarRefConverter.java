package me.ipid.jamelin.compiler;

import me.ipid.jamelin.ast.Ast.AstArrayAccess;
import me.ipid.jamelin.ast.Ast.AstMemberAccess;
import me.ipid.jamelin.ast.Ast.AstVarNameAccess;
import me.ipid.jamelin.ast.Ast.AstVarRef;
import me.ipid.jamelin.constant.PromelaLanguage.BinaryOp;
import me.ipid.jamelin.entity.CompileTimeInfo;
import me.ipid.jamelin.entity.il.*;
import me.ipid.jamelin.entity.sa.*;
import me.ipid.jamelin.exception.CompileExceptions.SyntaxException;
import me.ipid.util.errors.Unreachable;
import me.ipid.util.lateinit.LateInit;
import me.ipid.util.visitor.SubclassVisitor;

public class VarRefConverter {

    public static SATypedSlot buildTypedSlotOfVarRef(
            CompileTimeInfo cInfo, AstVarRef vRef
    ) {
        LateInit<SATypedSlot> result = new LateInit<>();

        SubclassVisitor.visit(
                vRef
        ).when(AstVarNameAccess.class, x -> {
            result.set(fromVarNameAccess(cInfo, x));
        }).when(AstArrayAccess.class, x -> {
            result.set(fromArrayAccess(cInfo, x));
        }).when(AstMemberAccess.class, x -> {
            result.set(fromMemberAccess(cInfo, x));
        }).other(x -> {
            throw new Unreachable();
        });

        return result.get();
    }

    public static ILRange buildRangeFromTypedSlot(
            SATypedSlot slot
    ) {
        int size = slot.type.getSize();
        ILExpr startIn = slot.combineOffset();
        ILExpr endEx = new ILBinaryExpr(startIn, new ILConstExpr(size), BinaryOp.ADD);

        return new ILRange(startIn, endEx, slot.global);
    }

    private static SATypedSlot fromArrayAccess(
            CompileTimeInfo cInfo, AstArrayAccess arrAccess
    ) {
        // 检查访问的是不是数组类型
        SATypedSlot slot = buildTypedSlotOfVarRef(cInfo, arrAccess.target);
        if (!(slot.type instanceof SAArrayType)) {
            throw new SyntaxException(slot.type.getName() + " 不是数组类型，不能用 [] 的形式访问");
        }

        // 获取数组类型（如 Foo[3]）
        var targetArr = (SAArrayType) slot.type;
        ILExpr indexExpr = ExprConverter.buildExpr(cInfo, arrAccess.index).expr;

        // 获取数组的基础元素的类型
        SAPromelaType arrOfType = targetArr.type;
        int arrBaseTypeSize = arrOfType.getSize();

        ILExpr newDOffset = slot.dOffset;
        int newSOffset = slot.sOffset;

        // 增加偏移：基础类型的 size * 访问的 index
        // 如果数组元素访问是动态的，就用 ILErrorExpr 包裹；否则产生编译时错误
        if (indexExpr instanceof ILConstExpr) {
            int index = ((ILConstExpr) indexExpr).num;
            if (index >= targetArr.arrLen) {
                throw new SyntaxException("数组 " + targetArr.getName() + " 的长度只有 " + targetArr.getSize() +
                        "，不存在第 " + index + " 号元素");
            }
            newSOffset += arrBaseTypeSize * ((ILConstExpr) indexExpr).num;
        } else {
            // 三元表达式：如果未越界，就返回偏移，否则报错
            ILExpr assertedDelta = new ILTernaryExpr(
                    new ILBinaryExpr(indexExpr, new ILConstExpr(targetArr.arrLen), BinaryOp.LESS),
                    new ILBinaryExpr(new ILConstExpr(arrBaseTypeSize), indexExpr, BinaryOp.MUL),
                    new ILErrorExpr("访问数组 " + targetArr.getName() + " 时越界")
            );
            newDOffset = new ILBinaryExpr(slot.dOffset, assertedDelta, BinaryOp.ADD);
        }

        return new SATypedSlot(arrOfType, slot.global, newSOffset, newDOffset);
    }

    private static SATypedSlot fromMemberAccess(
            CompileTimeInfo cInfo, AstMemberAccess memberAccess
    ) {
        // 获取目标的 Typed Slot
        SATypedSlot targetSlot = buildTypedSlotOfVarRef(cInfo, memberAccess.target);
        if (!(targetSlot.type instanceof SAUtype)) {
            throw new SyntaxException("类型 " + targetSlot.type.getName() + " 不是结构体，不能访问其 " +
                    memberAccess.member + " 属性");
        }
        var targetType = (SAUtype) targetSlot.type;

        // 获取目标字段
        var fieldRaw = targetType.fields.getGlobalVar(memberAccess.member);
        if (fieldRaw.isEmpty()) {
            throw new SyntaxException("类型 " + targetSlot.type.getName() + " 不存在名为 " +
                    memberAccess.member + " 的字段");
        }
        // 返回的 isGlobal
        SASymbolTableItem field = fieldRaw.get();

        return new SATypedSlot(field.type, targetSlot.global,
                targetSlot.sOffset + field.startAddr, targetSlot.dOffset);
    }

    private static SATypedSlot fromVarNameAccess(
            CompileTimeInfo cInfo, AstVarNameAccess vAccess
    ) {
        var vRaw = cInfo.table.getVar(vAccess.varName);
        if (vRaw.isEmpty()) {
            throw new SyntaxException("变量 " + vAccess.varName + " 不存在");
        }
        var targetTuple = vRaw.get();

        return new SATypedSlot(targetTuple.a.type, targetTuple.b, targetTuple.a.startAddr, new ILConstExpr(0));
    }
}
