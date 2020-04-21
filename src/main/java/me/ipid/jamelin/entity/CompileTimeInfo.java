package me.ipid.jamelin.entity;

import lombok.NonNull;
import me.ipid.jamelin.entity.sa.SASymbolTable;
import me.ipid.jamelin.entity.sa.SAPromelaType;

import java.util.Map;

public class CompileTimeInfo {
    public final SASymbolTable table;
    public final NamedItemContainer nItems;

    public CompileTimeInfo(@NonNull Map<String, SAPromelaType> initialTypes) {
        table = new SASymbolTable();
        nItems = new NamedItemContainer(initialTypes);
    }
}

