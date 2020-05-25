package me.ipid.jamelin.entity.sa;

import lombok.Data;
import lombok.NonNull;
import me.ipid.jamelin.constant.PromelaLanguage.BinaryOp;
import me.ipid.jamelin.entity.il.ILBinaryExpr;
import me.ipid.jamelin.entity.il.ILConstExpr;
import me.ipid.jamelin.entity.il.ILExpr;
import me.ipid.jamelin.entity.il.ILGetMemExpr;

public final @Data
class SATypedMemLoc {
    public final @NonNull SAPromelaType type;
    public final boolean global;
    public final int sOffset;
    public final @NonNull ILExpr dOffset;

    public ILGetMemExpr buildGetExpr() {
        if (!(type instanceof SAPrimitiveType)) {
            throw new Error("非原始类型；不能构建 get 表达式");
        }

        return new ILGetMemExpr(global, combineOffset());
    }

    public ILBinaryExpr combineOffset() {
        return new ILBinaryExpr(dOffset, new ILConstExpr(sOffset), BinaryOp.ADD);
    }
}
