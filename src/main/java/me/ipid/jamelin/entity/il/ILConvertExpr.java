package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.entity.ProcessControlBlock;
import me.ipid.jamelin.execute.JamelinKernel;
import me.ipid.util.errors.Unreachable;

public class ILConvertExpr implements ILExpr {

    public final boolean signed;
    public final int bitLen;
    public final ILExpr expr;

    public ILConvertExpr(boolean signed, int bitLen, ILExpr expr) {
        if (signed) {
            if (!is2Power(bitLen) || bitLen > 32 || bitLen < 8) {
                throw new Error("非法的 bitLen");
            }
        } else {
            if (bitLen < 1 || bitLen > 31) {
                throw new Error("非法的 bitLen");
            }
        }

        this.signed = signed;
        this.bitLen = bitLen;
        this.expr = expr;
    }

    /**
     * 判断数字是不是 2 的幂。
     */
    private static boolean is2Power(int num) {
        return (num & (num - 1)) == 0;
    }

    private int calcSigned(int newValue) {
        // 利用 JVM，实现带符号数据长度转换的功能
        if (bitLen == 8) {
            return (byte) newValue;
        } else if (bitLen == 16) {
            return (short) newValue;
        } else if (bitLen == 32) {
            return newValue;
        } else {
            throw new Unreachable();
        }
    }

    private int calcUnsigned(int newValue) {
        return newValue & ((1 << bitLen) - 1);
    }

    @Override
    public int execute(JamelinKernel kernel, ProcessControlBlock procInfo) {
        int value = expr.execute(kernel, procInfo);

        if (signed) {
            return calcSigned(value);
        } else {
            return calcUnsigned(value);
        }
    }
}
