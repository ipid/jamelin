package me.ipid.jamelin.entity;

import me.ipid.jamelin.constant.PromelaType;
import me.ipid.jamelin.entity.PromelaTypeInfo;

public class SymbolicTableItem {

    // 打包的变量信息
    private PromelaTypeInfo info;

    // true/false 表示是进程局部的/全局的变量
    private boolean isStackVar;

    // 指针，表示指向全局/栈上第几个变量
    private int ptr;

    public SymbolicTableItem(PromelaType type, int bitLen, int arrayLen, boolean isStackVar, int ptr) {
        this(
                new PromelaTypeInfo(type, bitLen, arrayLen),
                isStackVar, ptr
        );
    }

    public SymbolicTableItem(PromelaTypeInfo info, boolean isStackVar, int ptr) {
        this.info = info;
        this.isStackVar = isStackVar;
        this.ptr = ptr;
    }

    public PromelaType getType() {
        return info.getType();
    }

    public int getBitLen() {
        return info.getBitLen();
    }

    public int getArrayLen() {
        return info.getArrayLen();
    }

    public boolean isStackVar() {
        return isStackVar;
    }

    public int getPtr() {
        return ptr;
    }
}
