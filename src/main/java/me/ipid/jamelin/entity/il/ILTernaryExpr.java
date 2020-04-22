package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.execute.JamelinKernel;
import me.ipid.jamelin.execute.ProcessControlBlock;

public class ILTernaryExpr implements ILExpr {

    public final ILExpr condExpr, ifTrue, ifFalse;

    public ILTernaryExpr(ILExpr condExpr, ILExpr ifTrue, ILExpr ifFalse) {
        this.condExpr = condExpr;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
    }

    @Override
    public int execute(JamelinKernel kernel, ProcessControlBlock procInfo) {
        int cond = condExpr.execute(kernel, procInfo);
        if (cond != 0) {
            return ifTrue.execute(kernel, procInfo);
        } else {
            return ifFalse.execute(kernel, procInfo);
        }
    }
}
