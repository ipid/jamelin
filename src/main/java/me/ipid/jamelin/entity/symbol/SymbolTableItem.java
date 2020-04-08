package me.ipid.jamelin.entity.symbol;

import lombok.Data;
import me.ipid.jamelin.entity.il.ILType;

public @Data
class SymbolTableItem {
    public final ILType type;
    public final int startAddr;
    public final boolean isGlobal;
}
