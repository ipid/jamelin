package me.ipid.jamelin.entity.sa;

import me.ipid.jamelin.exception.CompileExceptions.SyntaxException;
import me.ipid.jamelin.util.Slot;
import me.ipid.util.tupling.Tuple2;

import java.util.*;

public final class SASymbolTable {
    private final List<LinkedHashMap<String, SASymbolTableItem>> historyLocalTables, symbolTables;

    private int globalStartAddr, localStartAddr;

    // 能否进入局部作用域
    private boolean localizable;

    public SASymbolTable() {
        this(true);
    }

    public SASymbolTable(boolean localizable) {
        this.historyLocalTables = new ArrayList<>();

        // 构造时先创建一张表，用于存放全局变量
        this.symbolTables = new ArrayList<>();
        this.symbolTables.add(new LinkedHashMap<>());

        this.globalStartAddr = this.localStartAddr = 0;
        this.localizable = localizable;
    }

    public int getGlobalLen() {
        return globalStartAddr;
    }

    public int getLocalLen() {
        if (!isInGlobal()) {
            throw new Error("必须在全局作用域中才能查询所有局部变量的长度");
        }

        return localStartAddr;
    }

    public boolean isInGlobal() {
        return symbolTables.size() == 1;
    }

    public boolean isInLocal() {
        return symbolTables.size() > 1;
    }

    public void enterScope() {
        if (isInGlobal()) {
            if (localStartAddr > 0 || historyLocalTables.size() > 0) {
                throw new Error("未重置局部变量，无法进入局部作用域");
            }
            if (!localizable) {
                throw new Error("本符号表实例不能进入局部作用域");
            }
        }

        var newTable = new LinkedHashMap<String, SASymbolTableItem>();
        symbolTables.add(newTable);
        historyLocalTables.add(newTable);
    }

    /**
     * 获取当前的符号表深度。
     * 如，在全局状态下为 0，进入 Proctype body 之后为 1。
     *
     * @return 语句块深度
     */
    public int getDepth() {
        // 全局时 size 为 1，所以要减 1
        return symbolTables.size() - 1;
    }

    public void exitScope() {
        // 试图退出全局作用域
        if (symbolTables.size() <= 1) {
            throw new Error("不能退出全局作用域");
        }

        symbolTables.remove(symbolTables.size() - 1);
    }

    public void fillGlobalMemory(List<? super Integer> list) {
        if (!isInGlobal()) {
            throw new Error("必须在全局作用域中才能填充全局内存");
        }

        fillMemoryOfTable(list, symbolTables.get(0));
    }

    public void fillHistoryLocalMemory(List<? super Integer> list) {
        if (!isInGlobal()) {
            throw new Error("必须在全局作用域中才能填充局部内存");
        }

        for (var table : historyLocalTables) {
            fillMemoryOfTable(list, table);
        }
    }

    /**
     * 等价于：调用 getVar，并断言获取到的是全局变量。
     * 如果存在同名的局部变量，将抛出 Error。
     *
     * @param name 变量名
     * @return 符号表项
     */
    public Optional<SASymbolTableItem> getGlobalVar(String name) {
        return getVar(name).map(x -> {
            assert x.b;
            return x.a;
        });
    }

    /**
     * 从符号表中查找指定变量。
     * 会返回该变量“是否是全局变量”的信息。
     *
     * @param name 变量名
     * @return 元组（Tuple2），内容为 (符号表项，是全局变量吗)
     */
    public Optional<Tuple2<SASymbolTableItem, Boolean>> getVar(String name) {
        // 从新的符号表开始，查询到旧的符号表上
        for (int i = symbolTables.size() - 1; i >= 0; i--) {
            SASymbolTableItem result = symbolTables.get(i).get(name);
            if (result != null) {
                return Optional.of(Tuple2.of(result, i <= 0));
            }
        }

        return Optional.empty();
    }

    public void putVar(String name, SAPromelaType type, SAInitVal initVal) {
        if (getVar(name).isPresent()) {
            throw new SyntaxException("变量 " + name + " 已存在，重复定义");
        }

        Map<String, SASymbolTableItem> table = symbolTables.get(symbolTables.size() - 1);

        // 获取并增加起始地址
        int currStartAddr;

        if (isInGlobal()) {
            currStartAddr = globalStartAddr;
            globalStartAddr += type.getSize();
        } else {
            currStartAddr = localStartAddr;
            localStartAddr += type.getSize();
        }

        table.put(name, new SASymbolTableItem(
                type, currStartAddr, initVal
        ));
    }

    public void resetLocal() {
        if (!isInGlobal()) {
            throw new Error("必须在全局作用域中才能重置局部变量");
        }

        localStartAddr = 0;
        historyLocalTables.clear();
    }

    public void fillSlots(List<? super Slot> slots) {
        // 只有在 utype 里使用的符号表才能 fillSlots（免得把全局的符号表 fill 了）
        assert !localizable;
        var table = symbolTables.get(0);

        for (SASymbolTableItem item : table.values()) {
            item.type.fillSlots(slots);
        }
    }

    private void fillMemoryOfTable(List<? super Integer> list, LinkedHashMap<String, SASymbolTableItem> table) {
        for (SASymbolTableItem item : table.values()) {
            item.initVal.visit(singleInit -> {
                // 单一初始值：直接放进去
                list.add(singleInit.num);

            }, initList -> {
                // 列表初始化：直接放进去
                for (int num : initList.nums) {
                    list.add(num);
                }

            }, noInit -> {
                if (item.type instanceof SAUtype) {
                    // Utype 的初始值不存在其上级符号表中，而是存在类型内部
                    var utype = (SAUtype) item.type;
                    utype.fields.fillGlobalMemory(list);

                } else if (item.type instanceof SAArrayType &&
                        ((SAArrayType) item.type).type instanceof SAUtype) {
                    // Utype Array 也要递归填写初始值
                    var saArr = (SAArrayType) item.type;
                    SAUtype arrOfUtype = (SAUtype) saArr.type;

                    for (int i = 0; i < saArr.arrLen; i++) {
                        arrOfUtype.fields.fillGlobalMemory(list);
                    }

                } else {
                    // 没有初始值，按照大小填 0
                    int size = item.type.getSize();

                    for (int i = 0; i < size; i++) {
                        list.add(0);
                    }
                }
            });
        }
    }
}
