package me.ipid.jamelin.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.ipid.jamelin.constant.PromelaType;
import me.ipid.jamelin.exception.SyntaxException;

import java.util.*;

public class SymbolicTable {
    private int staticVarNum, stackVarNum;

    private ArrayList<Map<String, SymbolicTableItem>> varTables;

    public SymbolicTable() {
        varTables = Lists.newArrayList(
                Maps.newHashMap()
        );
    }

    public void enterScope() {
        varTables.add(Maps.newHashMap());
        System.out.println("进入新作用域");
    }

    public void exitScope() {
        if (isInGlobalScope()) {
            throw new Error("现在已经是全局作用域，无法退出");
        }

        // 设置变量数目
        int currVarNum = varTables.get(varTables.size() - 1).size();
        stackVarNum -= currVarNum;

        // 弹出当前 Scope 的符号表（最后一个）
        varTables.remove(varTables.size() - 1);

        System.out.println("退出作用域");
    }

    public boolean isInGlobalScope() {
        return varTables.size() == 1;
    }

    public void newVariable(
            String name, PromelaType type,
            int bitLen, int arrayLen
    ) {
        // Promela 不允许变量重复定义
        if (findVar(name).isPresent()) {
            throw new SyntaxException(String.format("变量 %s 重复定义", name));
        }

        boolean isStackVar;
        int ptr;

        if (isInGlobalScope()) {
            isStackVar = false;
            staticVarNum++;
            ptr = staticVarNum;
        } else {
            isStackVar = true;
            stackVarNum++;
            ptr = stackVarNum;
        }

        SymbolicTableItem item = new SymbolicTableItem(
                type, bitLen, arrayLen, isStackVar, ptr
        );

        varTables.get(varTables.size() - 1).put(name, item);

        System.out.printf(
                "新变量：变量名 %s，类型 %s，长 %d 位，数组长度 %d",
                name, type.name(), bitLen, arrayLen);
    }

    public Optional<SymbolicTableItem> findVar(String name) {
        SymbolicTableItem varItem = null;

        for (int i = varTables.size() - 1; i >= 0; i--) {
            SymbolicTableItem result = varTables.get(i).get(name);
            if (result != null) {
                varItem = result;
                break;
            }
        }

        return Optional.ofNullable(varItem);
    }
}
