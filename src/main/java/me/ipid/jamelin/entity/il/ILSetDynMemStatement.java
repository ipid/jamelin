package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.entity.ProcessControlBlock;
import me.ipid.jamelin.execute.JamelinKernel;

public class ILSetDynMemStatement implements ILStatement {

    public final boolean global;
    public final ILExpr offsetExpr, valueExpr;

    public ILSetDynMemStatement(boolean global, ILExpr offsetExpr, ILExpr valueExpr) {
        this.global = global;
        this.offsetExpr = offsetExpr;
        this.valueExpr = valueExpr;
    }

    @Override
    public void execute(JamelinKernel kernel, ProcessControlBlock procInfo) {
        int value = valueExpr.execute(kernel, procInfo),
        offset = offsetExpr.execute(kernel, procInfo);

        throw new Error("TODO");
    }
}
