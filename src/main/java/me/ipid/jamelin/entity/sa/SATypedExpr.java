package me.ipid.jamelin.entity.sa;

import lombok.Data;
import lombok.NonNull;
import me.ipid.jamelin.entity.il.ILExpr;
import me.ipid.jamelin.exception.CompileExceptions.SyntaxException;

/**
 * 用来表示 IL 表达式的类型。
 * 与 SATypedSlot 不同之处在于，SATypedSlot 表示的是槽的位置，与获取该槽的 IL 表达式无关。
 */
public @Data
class SATypedExpr {
    public final @NonNull SAPromelaType type;
    public final @NonNull ILExpr expr;

    public ILExpr requirePrimitive() {
        if (!(type instanceof SAPrimitiveType)) {
            throw new SyntaxException("需要原始类型的表达式，然而类型是 " + type.getName());
        }

        return expr;
    }
}
