package me.ipid.jamelin.entity.symbol;

import com.google.common.collect.Lists;
import me.ipid.jamelin.entity.*;
import me.ipid.jamelin.entity.il.ILType;
import me.ipid.jamelin.exception.*;
import me.ipid.jamelin.exception.CompileExceptions.SyntaxException;

import java.util.*;

public class SymbolTable {
    private List<Map<String, SymbolTableItem>> historyLocalSymbolTables, symbolTables;

    private int globalStartAddr, localStartAddr;

    public SymbolTable() {
        Map<String, SymbolTableItem> table = new LinkedHashMap<>();
        historyLocalSymbolTables = Lists.newArrayList();
        symbolTables = Lists.newArrayList(table);

        globalStartAddr = localStartAddr = 0;
    }

    public void enterScope() {
        if (isInGlobal() && historyLocalSymbolTables.size() > 0) {
            throw new Error("历史符号表未清空，无法进入局部作用域");
        }

        Map<String, SymbolTableItem> table = new LinkedHashMap<>();
        historyLocalSymbolTables.add(table);
        symbolTables.add(table);
    }

    public void exitScope() {
        // 试图退出全局作用域
        if (symbolTables.size() <= 1) {
            throw new Error("不能退出全局作用域");
        }

        // 试图从局部退出到全局作用域
        if (symbolTables.size() == 2) {
            // 将局部地址清零
            localStartAddr = 0;
        }

        symbolTables.remove(symbolTables.size() - 1);
    }

    public void fillMemoryLayout(List<MemorySlot> container, boolean global) {
        if (global) {
            for (SymbolTableItem item : symbolTables.get(0).values()) {
                item.getType().fillMemoryLayout(container);
            }
            return;
        }

        for (Map<String, SymbolTableItem> table : historyLocalSymbolTables) {
            for (SymbolTableItem item : table.values()) {
                item.getType().fillMemoryLayout(container);
            }
        }
    }

    public void putVar(String name, ILType type) {
        if (getVar(name).isPresent()) {
            throw new SyntaxException(String.format("变量 %s 已存在，重复定义", name));
        }

        Map<String, SymbolTableItem> table = symbolTables.get(symbolTables.size() - 1);

        // 获取并增加起始地址
        int currStartAddr;
        boolean isGlobal;
        if (isInGlobal()) {
            currStartAddr = globalStartAddr;
            globalStartAddr += type.getSize();
            isGlobal = true;
        } else {
            currStartAddr = localStartAddr;
            localStartAddr += type.getSize();
            isGlobal = false;
        }

        table.put(name, new SymbolTableItem(
                type, currStartAddr, isGlobal
        ));
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

    public List<Map<String, SymbolTableItem>> clearHistoryLocal() {
        if (!isInGlobal()) {
            throw new Error("必须在全局作用域中才能清理历史记录");
        }

        List<Map<String, SymbolTableItem>> result = historyLocalSymbolTables;
        historyLocalSymbolTables = Lists.newArrayList();
        return result;
    }

    public boolean isInGlobal() {
        return symbolTables.size() == 1;
    }

    public boolean isInLocal() {
        return symbolTables.size() > 1;
    }
}
