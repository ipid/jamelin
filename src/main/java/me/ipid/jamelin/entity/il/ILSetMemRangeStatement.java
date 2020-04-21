package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.entity.ProcessControlBlock;
import me.ipid.jamelin.execute.JamelinKernel;

public class ILSetMemRangeStatement implements ILStatement {

    public final boolean global;
    public final ILExpr startInclusiveExpr, endExclusiveExpr, setToExpr;

    public ILSetMemRangeStatement(
            boolean global,
            ILExpr startInclusiveExpr, ILExpr endExclusiveExpr,
            ILExpr setToExpr
    ) {
        this.global = global;
        this.startInclusiveExpr = startInclusiveExpr;
        this.endExclusiveExpr = endExclusiveExpr;
        this.setToExpr = setToExpr;
    }

    @Override
    public void execute(JamelinKernel kernel, ProcessControlBlock procInfo) {
        int start = startInclusiveExpr.execute(kernel, procInfo),
                end = endExclusiveExpr.execute(kernel, procInfo),
                setTo = setToExpr.execute(kernel, procInfo);

        for (int i = start; i < end; i++) {
            if (global) {
                kernel.setGlobalMemory(i, setTo);
            }
        }
    }
}
