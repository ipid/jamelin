package me.ipid.jamelin.compiler;

import me.ipid.jamelin.ast.Ast.AstAdditionStatement;
import me.ipid.jamelin.ast.Ast.AstAssignment;
import me.ipid.jamelin.ast.Ast.AstSetValueStatement;
import me.ipid.jamelin.constant.PromelaLanguage.BinaryOp;
import me.ipid.jamelin.entity.CompileTimeInfo;
import me.ipid.jamelin.entity.il.*;
import me.ipid.jamelin.entity.sa.SAPrimitiveType;
import me.ipid.jamelin.entity.sa.SATypedMemLoc;
import me.ipid.jamelin.exception.CompileExceptions.SyntaxException;
import me.ipid.util.errors.Unreachable;

import java.util.ArrayList;
import java.util.List;

public final class AssignConverter {
    public static List<ILStatement> buildAssign(CompileTimeInfo cInfo, AstAssignment assign) {
        if (assign instanceof AstSetValueStatement) {
            return buildSetValue(cInfo, (AstSetValueStatement) assign);
        } else if (assign instanceof AstAdditionStatement) {
            return buildAddition(cInfo, (AstAdditionStatement) assign);
        } else {
            throw new Unreachable();
        }
    }

    private static List<ILStatement> buildAddition(CompileTimeInfo cInfo, AstAdditionStatement assign) {
        SATypedMemLoc slot = VarRefConverter.buildTypedMemLocOfVarRef(cInfo, assign.target);
        ILExpr valueAfterAdd = new ILBinaryExpr(
                slot.buildGetExpr(),
                new ILConstExpr(assign.addBy),
                BinaryOp.ADD
        );

        return genAssignStatements(slot, valueAfterAdd);
    }

    private static List<ILStatement> buildSetValue(CompileTimeInfo cInfo, AstSetValueStatement assign) {
        ILExpr value = ExprConverter.buildExpr(cInfo, assign.value).requirePrimitive();
        SATypedMemLoc slot = VarRefConverter.buildTypedMemLocOfVarRef(cInfo, assign.target);

        return genAssignStatements(slot, value);
    }

    private static void checkPrimitiveMemLoc(SATypedMemLoc typedSlot) {
        if (!(typedSlot.type instanceof SAPrimitiveType)) {
            throw new SyntaxException("试图使用 " + typedSlot.type.getName() +
                    " 类型的表达式来赋值，但 Promela 只允许整数类型的表达式");
        }
    }

    private static List<ILStatement> genAssignStatements(SATypedMemLoc slot, ILExpr value) {
        checkPrimitiveMemLoc(slot);
        var result = new ArrayList<ILStatement>();
        result.add(new ILSetMemStatement(slot.global, slot.combineOffset(), value));
        return result;
    }
}
