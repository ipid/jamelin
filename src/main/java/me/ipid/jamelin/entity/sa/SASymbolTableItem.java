package me.ipid.jamelin.entity.sa;

import lombok.Data;
import lombok.NonNull;

public @Data
class SASymbolTableItem {
    public final @NonNull SAPromelaType type;
    public final @NonNull int startAddr;
    public final @NonNull SAInitVal initVal;
}
