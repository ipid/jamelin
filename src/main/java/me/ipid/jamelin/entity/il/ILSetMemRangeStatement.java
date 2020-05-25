package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.execute.Kernel;
import me.ipid.jamelin.execute.ProcessControlBlock;

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
    public void execute(Kernel kernel, ProcessControlBlock procInfo) {
        int start = startInclusiveExpr.execute(kernel, procInfo, false),
                end = endExclusiveExpr.execute(kernel, procInfo, false),
                setTo = setToExpr.execute(kernel, procInfo, false);

        for (int i = start; i < end; i++) {
            if (global) {
                kernel.setGlobalMemory(i, setTo);
            }
        }
    }
}
