package me.ipid.jamelin.util;

import me.ipid.util.errors.Unreachable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NumberDowncaster {

    private static final Logger log = LogManager.getLogger(NumberDowncaster.class);

    public static int cast(boolean signed, int bitLen, int oldValue) {
        if (signed) {
            return castSigned(bitLen, oldValue);
        } else {
            return castUnsigned(bitLen, oldValue);
        }
    }

    public static int castSigned(int bitLen, int oldValue) {
        int newValue;

        // 利用 JVM，实现带符号数据长度转换的功能
        if (bitLen == 8) {
            newValue = (byte) oldValue;
        } else if (bitLen == 16) {
            newValue = (short) oldValue;
        } else if (bitLen == 32) {
            newValue = oldValue;
        } else {
            throw new Unreachable();
        }

        if (newValue != oldValue) {
            log.warn("数字 " + oldValue + " 被截断为 " + newValue);
        }

        return newValue;
    }

    public static int castUnsigned(int bitLen, int oldValue) {
        int newValue = oldValue & ((1 << bitLen) - 1);

        if (newValue != oldValue) {
            log.warn("数字 " + oldValue + " 被截断为 " + newValue);
        }

        return newValue;
    }
}
