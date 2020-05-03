package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.execute.JamelinKernel;
import me.ipid.jamelin.execute.ProcessControlBlock;

import java.util.List;

public class ILExprSendArg implements ILSendArgItem {
    public final ILExpr ilExpr;

    public ILExprSendArg(ILExpr ilExpr) {
        this.ilExpr = ilExpr;
    }

    @Override
    public void fillValue(
            JamelinKernel kernel, ProcessControlBlock pcb,
            List<? super Integer> target
    ) {
        target.add(ilExpr.execute(kernel, pcb, false));
    }
}
