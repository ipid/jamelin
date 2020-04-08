package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.constant.PromelaLanguage.UnaryOp;
import me.ipid.jamelin.entity.ProcessControlBlock;
import me.ipid.jamelin.execute.JamelinKernel;

public class ILUnaryExpr implements ILExpr {

    public final UnaryOp op;
    public final ILExpr target;

    public ILUnaryExpr(UnaryOp op, ILExpr target) {
        this.op = op;
        this.target = target;
    }

    @Override
    public int execute(JamelinKernel kernel, ProcessControlBlock procInfo) {
        int num = target.execute(kernel, procInfo);

        switch (op) {
            case BIT_NOT:
                return ~num;
            case LOGIC_NOT:
                return num != 0 ? 0 : 1;
            case OPPOSITE_NUM:
                return -num;
        }

        return num;
    }
}
