package me.ipid.jamelin.compiler;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.ipid.jamelin.entity.*;
import me.ipid.jamelin.exception.SyntaxException;

import java.util.*;

public class ScopeManager {
    private List<Map<String, SymbolTableItem>> historySymbolTables, symbolTables;

    private int currStartAddr = 0;

    public ScopeManager() {
        Map<String, SymbolTableItem> table = new HashMap<>();
        historySymbolTables = Lists.newArrayList(table);
        symbolTables = Lists.newArrayList(table);
    }

    public List<Map<String, SymbolTableItem>> getHistorySymbolTables() {
        return historySymbolTables;
    }

    public List<Map<String, SymbolTableItem>> getSymbolTables() {
        return symbolTables;
    }

    public void enterScope() {
        Map<String, SymbolTableItem> table = new HashMap<>();
        historySymbolTables.add(table);
        symbolTables.add(table);
    }

    public void exitScope() {
        if (symbolTables.size() == 1) {
            throw new Error("不能退出全局作用域");
        }

        symbolTables.remove(symbolTables.size() - 1);
    }

    public void fillMemoryLayout(List<MemorySlot> container) {
        throw new Error();
    }

    public void putVar(String name, PromelaType type) {
        if(getVar(name).isPresent()) {
            throw new SyntaxException(String.format("变量 %s 已存在，重复定义", name));
        }

        Map<String, SymbolTableItem> table = symbolTables.get(symbolTables.size() - 1);
        table.put(name, new SymbolTableItem(
                type, currStartAddr
        ));

        currStartAddr += type.getSize();
    }

    public Optional<SymbolTableItem> getVar(String name) {
        for (int i = symbolTables.size() - 1; i >= 0; i--) {
            SymbolTableItem result = symbolTables.get(i).get(name);
            if (result != null) {
                return Optional.of(result);
            }
        }

        return Optional.empty();
    }
}
