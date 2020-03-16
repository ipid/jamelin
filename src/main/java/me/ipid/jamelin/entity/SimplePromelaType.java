package me.ipid.jamelin.entity;

import java.util.List;

public class SimplePromelaType implements PromelaType {
    // 是否是有符号的
    private boolean signed;

    // 变量数据长度（单位：bits），包括符号位
    private int bitLen;

    // 数组长度，小于 0 表示不是数组
    private int arrayLen;

    public SimplePromelaType(boolean signed, int bitLen, int arrayLen) {
        this.signed = signed;
        this.bitLen = bitLen;
        this.arrayLen = arrayLen;
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
        if (bitLen < 0) {
            container.add(new MemorySlot(signed, bitLen));
        } else {
            for (int i = 0; i < arrayLen; i++) {
                container.add(new MemorySlot(signed, bitLen));
            }
        }
    }
}
