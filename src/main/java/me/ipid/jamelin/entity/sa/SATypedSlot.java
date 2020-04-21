package me.ipid.jamelin.entity.sa;

import lombok.Data;
import lombok.NonNull;
import me.ipid.jamelin.constant.PromelaLanguage.BinaryOp;
import me.ipid.jamelin.entity.il.ILBinaryExpr;
import me.ipid.jamelin.entity.il.ILConstExpr;
import me.ipid.jamelin.entity.il.ILExpr;

public final @Data
class SATypedSlot {
    public final @NonNull SAPromelaType type;
    public final boolean global;
    public final int sOffset;
    public final @NonNull ILExpr dOffset;

    public ILExpr combineOffset() {
        return new ILBinaryExpr(dOffset, new ILConstExpr(sOffset), BinaryOp.ADD);
    }
}
