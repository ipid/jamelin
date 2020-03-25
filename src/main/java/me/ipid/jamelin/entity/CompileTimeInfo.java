package me.ipid.jamelin.entity;

import me.ipid.jamelin.entity.symbol.*;
import org.apache.commons.collections4.map.ListOrderedMap;

public class CompileTimeInfo {
    public final SymbolTable table;
    public final ListOrderedMap<String, Proctype> proctypes;

    public CompileTimeInfo() {
        table = new SymbolTable();
        proctypes = new ListOrderedMap<>();
    }
}

