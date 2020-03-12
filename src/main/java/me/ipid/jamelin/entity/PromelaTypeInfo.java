package me.ipid.jamelin.entity;

import me.ipid.jamelin.constant.PromelaType;

public class PromelaTypeInfo {
    // 变量类型
    private PromelaType type;

    // 变量数据长度（单位：bits）
    private int bitLen;

    // 数组长度，小于 0 表示不是数组
    private int arrayLen;

    public PromelaTypeInfo(PromelaType type, int bitLen, int arrayLen) {
        this.type = type;
        this.bitLen = bitLen;
        this.arrayLen = arrayLen;
    }

    public PromelaType getType() {
        return type;
    }

    public int getBitLen() {
        return bitLen;
    }

    public int getArrayLen() {
        return arrayLen;
    }
}
