package me.ipid.jamelin.entity;

public class MemorySlot implements Cloneable {
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
        if (signed) {
            assignSigned(value);
        } else {
            assignUnsigned(value);
        }

        return this;
    }

    private void assignSigned(int newValue) {
        // 利用 JVM，实现带符号数据长度转换的功能
        if (bitLen == 8) {
            value = (byte) newValue;
        } else if (bitLen == 16) {
            value = (short) newValue;
        } else if (bitLen == 32) {
            value = newValue;
        } else {
            throw new Error("bitLen 非法");
        }
    }

    private void assignUnsigned(int newValue) {
        value = newValue & ((1 << bitLen) - 1);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
