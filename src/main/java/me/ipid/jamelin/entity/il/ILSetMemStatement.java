package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.execute.Kernel;
import me.ipid.jamelin.execute.ProcessControlBlock;

public class ILSetMemStatement implements ILStatement {

    public final boolean global;
    public final ILExpr offsetExpr, valueExpr;

    public ILSetMemStatement(boolean global, ILExpr offsetExpr, ILExpr valueExpr) {
        this.global = global;
        this.offsetExpr = offsetExpr;
        this.valueExpr = valueExpr;
    }

    @Override
    public void execute(Kernel kernel, ProcessControlBlock procInfo) {
        int value = valueExpr.execute(kernel, procInfo, false),
                offset = offsetExpr.execute(kernel, procInfo, false);

        if (global) {
            kernel.setGlobalMemory(offset, value);
        } else {
            procInfo.setProcessMemory(offset, value);
        }
    }
}
