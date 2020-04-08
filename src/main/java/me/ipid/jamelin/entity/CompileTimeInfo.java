package me.ipid.jamelin.entity;

import me.ipid.jamelin.entity.il.ILNamedItem;
import me.ipid.jamelin.entity.il.ILProctype;
import me.ipid.jamelin.entity.symbol.*;
import org.apache.commons.collections4.map.ListOrderedMap;

import java.util.HashMap;
import java.util.Map;

public class CompileTimeInfo {
    public final SymbolTable table;
    public final Map<String, ILNamedItem> namedItems;
    public final ListOrderedMap<String, ILProctype> proctypes;

    public CompileTimeInfo() {
        table = new SymbolTable();
        namedItems = new HashMap<>();
        proctypes = new ListOrderedMap<>();
    }
}

