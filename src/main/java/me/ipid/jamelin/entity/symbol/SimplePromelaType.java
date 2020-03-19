package me.ipid.jamelin.entity.symbol;

import me.ipid.jamelin.entity.*;

import java.util.List;

public class SimplePromelaType implements PromelaType, PromelaNamedItem {
    // 是否是有符号的
    private boolean signed;

    // 变量数据长度（单位：bits），包括符号位
    private int bitLen;

    // 数组长度，小于 0 表示不是数组
    private int arrayLen;

    // 初始值，供后续重构使用
    private int initialValue;

    public SimplePromelaType(boolean signed, int bitLen, int arrayLen) {
        this.signed = signed;
        this.bitLen = bitLen;
        this.arrayLen = arrayLen;
        this.initialValue = 0;
    }

    public SimplePromelaType(boolean signed, int bitLen, int arrayLen, int initialValue) {
        this.signed = signed;
        this.bitLen = bitLen;
        this.arrayLen = arrayLen;
        this.initialValue = initialValue;
    }

    @Override
    public int getSize() {
        if (arrayLen < 0) {
            return 1;
        }
        return arrayLen;
    }

    @Override
    public void fillMemoryLayout(List<MemorySlot> container) {
        if (arrayLen < 0) {
            container.add(new MemorySlot(signed, bitLen, initialValue));
        } else {
            for (int i = 0; i < arrayLen; i++) {
                container.add(new MemorySlot(signed, bitLen, initialValue));
            }
        }
    }
}
