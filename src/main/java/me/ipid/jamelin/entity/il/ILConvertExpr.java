package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.execute.Kernel;
import me.ipid.jamelin.execute.ProcessControlBlock;
import me.ipid.jamelin.util.NumberDowncaster;

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

    @Override
    public int execute(Kernel kernel, ProcessControlBlock procInfo, boolean noSideEffect) {
        int value = expr.execute(kernel, procInfo, noSideEffect);

        if (signed) {
            return NumberDowncaster.castSigned(bitLen, value);
        } else {
            return NumberDowncaster.castUnsigned(bitLen, value);
        }
    }
}
