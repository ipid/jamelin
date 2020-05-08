package me.ipid.jamelin.compiler;

import lombok.NonNull;
import me.ipid.jamelin.ast.Ast.*;
import me.ipid.jamelin.entity.CompileTimeInfo;
import me.ipid.jamelin.entity.il.*;
import me.ipid.jamelin.entity.sa.*;
import me.ipid.jamelin.entity.sa.SATypeFactory.PrimitiveTypesLib;
import me.ipid.jamelin.exception.CompileExceptions.NotSupportedException;
import me.ipid.jamelin.exception.CompileExceptions.SyntaxException;
import me.ipid.jamelin.util.Slot;
import me.ipid.util.cell.Cell;
import me.ipid.util.cell.Cells;
import me.ipid.util.errors.Unreachable;
import me.ipid.util.visitor.SubclassVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 职责：
 * - 处理变量声明、变量赋值的 AST
 * - 生成代码，并尽可能通过返回值的方式返回信息
 */
@SuppressWarnings("OptionalGetWithoutIsPresent")
public final class DeclareConverter {

    public static void addUtype(CompileTimeInfo cInfo, AstUtype astUtype) {
        // 检查名字是否冲突
        cInfo.checkNameExist(astUtype.newTypeName);

        // 创建 utype 对象
        SAUtype saUtype = new SAUtype(astUtype.newTypeName, SATypeFactory.allocTypeId());

        // 逐条处理 AST 中的声明
        for (AstDeclare astDeclare : astUtype.members) {
            // 检查字段名是否重复
            if (saUtype.fields.getVar(astDeclare.getVarName()).isPresent()) {
                throw new SyntaxException(
                        "自定义类型 " + astUtype.newTypeName + " 错误：字段 " + astDeclare.getVarName() + " 已经存在");
            }

            // 提取类型、初值
            SAPromelaType fieldType = extractType(cInfo, astDeclare);
            var saInit = GlbInitValConverter.buildInitValInUtype(fieldType, astDeclare.getVarInit());

            // 放入当前 utype 的符号表中
            saUtype.fields.putVar(astDeclare.getVarName(), fieldType, saInit);
        }

        // 放入全局符号表中
        cInfo.nItems.putType(astUtype.newTypeName, saUtype);
    }

    public static List<ILStatement> buildFromDeclare(
            @NonNull CompileTimeInfo cInfo, @NonNull AstDeclare declare
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

    private static List<ILStatement> assignExprOnOffset(
            SASymbolTableItem tableItem,
            boolean isGlobal,
            ILExpr setTo
    ) {
        ILExpr ilOffset = new ILConstExpr(tableItem.startAddr);

        var result = new ArrayList<ILStatement>();
        result.add(new ILSetMemStatement(isGlobal, ilOffset, setTo));

        return result;
    }

    private static ILCrateChanExpr buildChanInit(CompileTimeInfo cInfo, AstChanInit astInit) {
        List<Slot> slots = new ArrayList<>();
        List<Integer> typeIds = new ArrayList<>(), msgUnitLen = new ArrayList<>();

        // 遍历类型，生成槽和 TypeID 数组
        for (String typeName : astInit.elemTypeTuple) {
            // 获取类型对象并验证
            var saTypeRaw = cInfo.nItems.getItem(typeName);
            if (saTypeRaw.isEmpty()) {
                throw new SyntaxException("信道初始化时使用的 " + typeName + " 类型不存在");
            } else if (!(saTypeRaw.get() instanceof SAPromelaType)) {
                throw new SyntaxException("信道初始化时使用的 " + typeName + " 类型不是类型的名字");
            }

            SAPromelaType saType = (SAPromelaType) saTypeRaw.get();

            // 获取其槽、typeid、长度
            saType.fillSlots(slots);
            msgUnitLen.add(saType.getSize());

            // 如果该类型是原始类型，则此处用 int 的 typeid 来代替
            if (saType instanceof SAPrimitiveType) {
                typeIds.add(PrimitiveTypesLib.int_t.getTypeId());
            } else {
                typeIds.add(saType.getTypeId());
            }
        }

        assert msgUnitLen.stream().mapToInt(x -> x).sum() == slots.size();
        return new ILCrateChanExpr(astInit.bufLen, slots, typeIds, msgUnitLen);
    }

    private static List<ILStatement> buildFromNormalDeclare(
            CompileTimeInfo cInfo, AstNormalDeclare declare
    ) {
        if (declare.show || declare.local || declare.hidden) {
            throw new NotSupportedException("这编译器菜得很，不支持 show、local、hidden 这些高端特性");
        }

        // 检查是否重名
        cInfo.checkNameExist(declare.varName);

        // 提取类型，插入符号表
        SAPromelaType saType = extractType(cInfo, declare);

        // 没有初始值
        if (declare.varInit.isEmpty()) {
            cInfo.table.putVar(declare.varName, saType, SANoInit.instance());
            return new ArrayList<>();
        }
        var astInit = declare.varInit.get();

        // Primitive Array - Init List
        if (astInit instanceof AstInitializerList && saType.isPrimitiveArray()) {
            SAInitVal initVal = GlbInitValConverter.initListOnArray(
                    (SAArrayType) saType, (AstInitializerList) astInit
            );
            cInfo.table.putVar(declare.varName, saType, initVal);
            return new ArrayList<>();
        }

        // Primitive Type / Primitive Array - Expr Init
        if (astInit instanceof AstExprAsInit || astInit instanceof AstChanInit) {
            ILExpr ilValue;
            if (astInit instanceof AstExprAsInit) {
                ilValue = ExprConverter.buildExpr(cInfo, ((AstExprAsInit) astInit).expr).requirePrimitive();
            } else {
                ilValue = buildChanInit(cInfo, (AstChanInit) astInit);
            }

            // Primitive Type - Expr Init
            if (saType instanceof SAPrimitiveType) {
                cInfo.table.putVar(declare.varName, saType, SANoInit.instance());
                var item = cInfo.table.getVar(declare.varName).get();

                return assignExprOnOffset(
                        item.a, item.b, ilValue);

            } else if (saType.isPrimitiveArray()) {
                cInfo.table.putVar(declare.varName, saType, SANoInit.instance());
                var item = cInfo.table.getVar(declare.varName).get();

                return memsetExprOnRange(
                        item.a, item.b, ilValue);
            }
        }

        throw new SyntaxException("类型 " + saType.getName() +
                " 不能用 " + astInit.getClass().getSimpleName() + " 来初始化");
    }

    private static List<ILStatement> buildFromUnsignedDeclare(CompileTimeInfo cInfo, AstUnsignedDeclare dec) {
        // 检查是否重名
        cInfo.checkNameExist(dec.varName);

        // 放入符号表
        var type = extractUnsigned(dec);
        cInfo.table.putVar(dec.varName, type, SANoInit.instance());
        var item = cInfo.table.getVar(dec.varName).get();

        // 生成赋值语句
        if (dec.varInit.isEmpty()) {
            return new ArrayList<>();
        }
        var initVal = dec.varInit.get();
        if (initVal instanceof AstExprAsInit) {
            // 生成一条简单的赋值语句
            var exprInitVal = (AstExprAsInit) initVal;
            ILExpr ilValue = ExprConverter.buildExpr(cInfo, exprInitVal.expr).expr;

            return assignExprOnOffset(
                    item.a, item.b, ilValue);
        }

        throw new SyntaxException("unsigned 变量不能用 " + initVal.getClass().getSimpleName() + " 初始化");
    }

    private static SAPromelaType extractNormalDeclare(CompileTimeInfo cInfo, AstNormalDeclare decl) {
        Optional<ILNamedItem> typeRaw = cInfo.nItems.getItem(decl.typeName);
        if (typeRaw.isEmpty()) {
            throw new SyntaxException("名为 " + decl.typeName + " 的类型不存在");
        } else if (!(typeRaw.get() instanceof SAPromelaType)) {
            throw new SyntaxException(decl.typeName + " 不是类型名。内部表示为：" +
                    typeRaw.get().getClass().getSimpleName());
        }

        SAPromelaType saType = (SAPromelaType) typeRaw.get();
        // 如果是数组，就上一层 wrapper
        if (decl.arrayLen == 0) {
            throw new SyntaxException("Promela 不允许零长数组");
        } else if (decl.arrayLen > 0) {
            saType = new SAArrayType(saType, decl.arrayLen);
        }

        return saType;
    }

    private static SAPromelaType extractType(CompileTimeInfo cInfo, AstDeclare declare) {
        Cell<SAPromelaType> result = Cells.empty();

        SubclassVisitor.visit(
                declare
        ).when(AstNormalDeclare.class, x -> {
            result.v = extractNormalDeclare(cInfo, x);
        }).when(AstUnsignedDeclare.class, x -> {
            result.v = extractUnsigned(x);
        }).other(x -> {
            throw new Unreachable();
        });

        return result.v;
    }

    private static SAPrimitiveType extractUnsigned(AstUnsignedDeclare decl) {
        return new SAPrimitiveType(
                "unsigned", false, decl.bitLen, SATypeFactory.allocTypeIdForUnsigned(decl.bitLen)
        );
    }

    private static List<ILStatement> memsetExprOnRange(
            SASymbolTableItem tableItem,
            boolean isGlobal,
            ILExpr setTo
    ) {
        assert tableItem.type.isPrimitiveArray();

        ILExpr startIn = new ILConstExpr(tableItem.startAddr),
                endEx = new ILConstExpr(tableItem.startAddr + tableItem.type.getSize());

        var result = new ArrayList<ILStatement>();
        result.add(new ILSetMemRangeStatement(isGlobal, startIn, endEx, setTo));

        return result;
    }
}
