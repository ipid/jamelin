package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.execute.JamelinKernel;
import me.ipid.jamelin.execute.ProcessControlBlock;

public class ILSetMemStatement implements ILStatement {

    private boolean global;
    private int offset;
    private ILExpr valueExpr;

    public ILSetMemStatement(boolean global, int offset, ILExpr valueExpr) {
        this.global = global;
        this.offset = offset;
        this.valueExpr = valueExpr;
    }

    public int getOffset() {
        return offset;
    }

    public ILExpr getValueExpr() {
        return valueExpr;
    }

    public boolean isGlobal() {
        return global;
    }

    @Override
    public void execute(JamelinKernel kernel, ProcessControlBlock procInfo) {
        int value = valueExpr.execute(kernel, procInfo);

        if (global) {
            kernel.setGlobalMemory(offset, value);
        } else {
            procInfo.setProcessMemory(offset, value);
        }
    }
}
