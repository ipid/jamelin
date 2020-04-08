package me.ipid.jamelin.compiler;

import com.google.common.collect.ImmutableMap;
import me.ipid.jamelin.ast.Ast.AstDeclare;
import me.ipid.jamelin.ast.Ast.AstExprAsInit;
import me.ipid.jamelin.ast.Ast.AstNormalDeclare;
import me.ipid.jamelin.ast.Ast.AstUnsignedDeclare;
import me.ipid.jamelin.entity.CompileTimeInfo;
import me.ipid.jamelin.entity.il.*;
import me.ipid.jamelin.exception.CompileExceptions.NotSupportedException;
import me.ipid.jamelin.exception.Unreachable;
import me.ipid.jamelin.thirdparty.antlr.PromelaAntlrBaseVisitor;
import me.ipid.util.cell.Cell;
import me.ipid.util.cell.Cells;
import me.ipid.util.visitor.SubclassVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 职责：
 * - 处理变量声明、变量赋值的 AST
 * - 生成代码，并尽可能通过返回值的方式返回信息
 */
public class DeclareConverter
        extends PromelaAntlrBaseVisitor<List<ILStatement>> {

    private static final Map<String, ILSimpleType> simpleTypeMap =
            new ImmutableMap.Builder<String, ILSimpleType>()
                    .put("bit", new ILSimpleType(false, 1, 0, "bit"))
                    .put("bool", new ILSimpleType(false, 1, 0, "bool"))
                    .put("byte", new ILSimpleType(false, 8, 0, "byte"))
                    .put("chan", new ILSimpleType(false, 8, 0, "chan"))
                    .put("short", new ILSimpleType(true, 16, 0, "short"))
                    .put("int", new ILSimpleType(true, 32, 0, "int"))
                    .put("mtype", new ILSimpleType(false, 8, 0, "mtype"))
                    .put("pid", new ILSimpleType(false, 8, 0, "pid"))
                    .build();

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public static List<ILStatement> buildFromDeclare(
            CompileTimeInfo cInfo, AstDeclare declare
    ) {
        Cell<List<ILStatement>> result = Cells.empty();

        SubclassVisitor.visit(
                declare
        ).when(AstNormalDeclare.class, x -> {
            result.v = buildFromNormalDeclare(cInfo, x);
        }).when(AstUnsignedDeclare.class, x -> {
            result.v = buildFromUnsignedDeclare(cInfo, x);
        }).other(x -> {
            throw new Unreachable();
        });

        return result.v;
    }

    private static List<ILStatement> buildFromUnsignedDeclare(CompileTimeInfo cInfo, AstUnsignedDeclare dec) {
        throw new NotSupportedException("暂不支持 unsigned 变量");
    }

    private static List<ILStatement> buildFromNormalDeclare(
            CompileTimeInfo cInfo, AstNormalDeclare declare
    ) {
        if (declare.show || declare.local || declare.hidden) {
            throw new NotSupportedException("这编译器菜得很，不支持 show、local、hidden 这些高端特性");
        }

        ILType ilType = simpleTypeMap.get(declare.typeName);
        // 如果类型不是简单类型
        if (ilType == null) {
            throw new NotSupportedException("别折腾了，这编译器不支持高端自定义类型");
        }
        // 如果是数组，就上一层 wrapper
        if (declare.arrayLen > 0) {
            ilType = new ILArrayType(declare.arrayLen, ilType);
        }
        cInfo.table.putVar(declare.varName, ilType);

        List<ILStatement> result = new ArrayList<>();
        var tableItem = cInfo.table.getVar(declare.varName).get();

        declare.varInit.ifPresent(astVarInit -> SubclassVisitor.visit(
                astVarInit
        ).when(AstExprAsInit.class, x -> {
            ILExpr initValue = ExprConverter.buildExpr(cInfo, x.expr);
            result.add(new ILSetMemExpr(tableItem.isGlobal, tableItem.startAddr, initValue));
        }).other(x -> {
            throw new NotSupportedException("暂不支持 " + x.getClass().getSimpleName());
        }));

        return result;
    }
}
