package me.ipid.jamelin.compiler;

import me.ipid.jamelin.ast.Ast.*;
import me.ipid.jamelin.entity.sa.*;
import me.ipid.jamelin.exception.CompileExceptions.NotSupportedException;
import me.ipid.jamelin.exception.CompileExceptions.SyntaxException;
import me.ipid.util.errors.Unreachable;
import me.ipid.util.lateinit.LateInit;
import me.ipid.util.visitor.SubclassVisitor;

import java.util.Optional;
import java.util.stream.IntStream;

public class GlbInitValConverter {
    /**
     * 为 Utype 中的初值定义，生成 SAInitVal 对象。
     * 注意：
     * - 请不要在全局变量、局部变量初始化的时候使用本函数。
     * - 如果传入的 saType 是数组类型，返回值保证为 SAInitList
     *
     * @return SAInitVal
     */
    public static SAInitVal buildInitValInUtype(SAPromelaType saType, Optional<AstVarInit> astInitRaw) {
        if (astInitRaw.isEmpty()) {
            return new SANoInit();
        }
        if (!(saType instanceof SAPrimitiveType || saType.isPrimitiveArray())) {
            throw new SyntaxException("类型 " + saType.getName() + " 不允许设置初值");
        }

        AstVarInit astInit = astInitRaw.get();
        var result = new LateInit<SAInitVal>();

        // { chan, expr, init-list } * { arr, primitive }
        SubclassVisitor.visit(
                astInit
        ).when(AstChanInit.class, ast -> {
            // chan 初始化
            throw new NotSupportedException("暂不支持 chan 初始化");

        }).when(AstExprAsInit.class, ast -> {
            // 单个表达式初始化

            if (!(ast.expr instanceof AstConstExpr)) {
                throw new SyntaxException("在 utype 中只允许用常量表达式来初始化");
            }
            int num = ((AstConstExpr) ast.expr).num;
            result.set(singleNumOnAnyType(saType, num));

        }).when(AstInitializerList.class, initList -> {
            // 多个表达式初始化（aka 初始化列表）
            if (saType instanceof SAPrimitiveType) {
                throw new SyntaxException("原始类型不能用初始化列表来初始化");
            }

            result.set(initListOnArray((SAArrayType)saType, initList));

        }).other(x -> {
            throw new Unreachable();
        });

        return result.get();
    }

    private static SAInitVal singleNumOnAnyType(SAPromelaType saType, int num) {
        LateInit<SAInitVal> result = new LateInit<>();

        SubclassVisitor.visit(
                saType
        ).when(SAArrayType.class, saArr -> {
            // 如果类型为数组，就将 num 重复 saArr 中的数组长度那么多遍
            result.set(new SAInitList(
                    IntStream.range(0, saArr.arrLen).map(anything -> num).toArray()
            ));
        }).when(SAPrimitiveType.class, saPrim -> {
            // 如果类型为 primitive，那也太简单了
            result.set(new SASingleInitVal(num));
        }).other(anything -> {
            throw new Unreachable();
        });

        return result.get();
    }

    public static SAInitList initListOnArray(SAArrayType saArr, AstInitializerList initList) {
        assert saArr.isPrimitiveArray();

        // initList 可以比数组长度短，多余部分是 0
        if (initList.exprs.size() > saArr.arrLen) {
            throw new SyntaxException("初始化列表中的表达式太多，多于数组长度");
        }

        // arr 中多余的部分自动置为 0，与 C 语言的逻辑一致
        int[] arr = new int[saArr.arrLen];
        for (int i = 0; i < initList.exprs.size(); i++) {
            if (!(initList.exprs.get(i) instanceof AstConstExpr)) {
                throw new SyntaxException("数组用初始化列表初始化时，列表中的值必须是常量");
            }

            arr[i] = ((AstConstExpr)initList.exprs.get(i)).num;
        }

        return new SAInitList(arr);
    }
}
