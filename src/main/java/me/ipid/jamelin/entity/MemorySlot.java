package me.ipid.jamelin.entity;

import me.ipid.jamelin.exception.NotSupportedException;

public class MemorySlot {
    private boolean signed;
    private int bitLen;
    private int value;

    public MemorySlot(boolean signed, int bitLen) {
        this.signed = signed;
        this.bitLen = bitLen;
        this.value = 0;
    }

    public MemorySlot(boolean signed, int bitLen, int value) {
        this.signed = signed;
        this.bitLen = bitLen;
        this.value = value;
    }

    public boolean isSigned() {
        return signed;
    }

    public int getBitLen() {
        return bitLen;
    }

    public int getValue() {
        return value;
    }

    public MemorySlot setValue(int value) {
        throw new Error();
    }
}
