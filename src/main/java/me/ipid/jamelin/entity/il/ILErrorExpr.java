package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.exception.RuntimeExceptions.JamelinRuntimeException;
import me.ipid.jamelin.execute.JamelinKernel;
import me.ipid.jamelin.execute.ProcessControlBlock;

public class ILErrorExpr implements ILExpr {
    public final String errMsg;

    public ILErrorExpr(String errMsg) {
        this.errMsg = errMsg;
    }

    @Override
    public int execute(JamelinKernel kernel, ProcessControlBlock procInfo) {
        throw new JamelinRuntimeException(errMsg);
    }
}
