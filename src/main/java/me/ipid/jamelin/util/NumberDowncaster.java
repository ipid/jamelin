package me.ipid.jamelin.util;

import me.ipid.util.errors.Unreachable;

public class NumberDowncaster {
    public static int castSigned(int bitLen, int oldValue) {
        // 利用 JVM，实现带符号数据长度转换的功能
        if (bitLen == 8) {
            return (byte) oldValue;
        } else if (bitLen == 16) {
            return (short) oldValue;
        } else if (bitLen == 32) {
            return oldValue;
        } else {
            throw new Unreachable();
        }
    }

    public static int castUnsigned(int bitLen, int oldValue) {
        return oldValue & ((1 << bitLen) - 1);
    }
}
