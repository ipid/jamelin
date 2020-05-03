package me.ipid.jamelin.util;

public class Slot {
    public final int bitLen;
    public final boolean signed;

    public Slot(int bitLen, boolean signed) {
        this.bitLen = bitLen;
        this.signed = signed;
    }

    public int cast(int oldValue) {
        return NumberDowncaster.cast(signed, bitLen, oldValue);
    }
}
