package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.execute.Kernel;
import me.ipid.jamelin.execute.ProcessControlBlock;

import java.util.List;

public class ILExprSendArg implements ILSendArgItem {
    public final ILExpr ilExpr;

    public ILExprSendArg(ILExpr ilExpr) {
        this.ilExpr = ilExpr;
    }

    @Override
    public void fillValue(
            Kernel kernel, ProcessControlBlock pcb,
            List<? super Integer> target
    ) {
        target.add(ilExpr.execute(kernel, pcb, false));
    }
}
