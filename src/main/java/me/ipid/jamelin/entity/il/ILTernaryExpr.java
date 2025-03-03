package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.execute.Kernel;
import me.ipid.jamelin.execute.ProcessControlBlock;

public class ILTernaryExpr implements ILExpr {

    public final ILExpr condExpr, ifTrue, ifFalse;

    public ILTernaryExpr(ILExpr condExpr, ILExpr ifTrue, ILExpr ifFalse) {
        this.condExpr = condExpr;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
    }

    @Override
    public int execute(Kernel kernel, ProcessControlBlock procInfo, boolean noSideEffect) {
        int cond = condExpr.execute(kernel, procInfo, noSideEffect);
        if (cond != 0) {
            return ifTrue.execute(kernel, procInfo, noSideEffect);
        } else {
            return ifFalse.execute(kernel, procInfo, noSideEffect);
        }
    }
}
