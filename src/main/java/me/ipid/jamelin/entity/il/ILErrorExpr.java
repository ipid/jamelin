package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.exception.RuntimeExceptions.JamelinRuntimeException;
import me.ipid.jamelin.execute.Kernel;
import me.ipid.jamelin.execute.ProcessControlBlock;

public class ILErrorExpr implements ILExpr {
    public final String errMsg;

    public ILErrorExpr(String errMsg) {
        this.errMsg = errMsg;
    }

    @Override
    public int execute(Kernel kernel, ProcessControlBlock procInfo, boolean noSideEffect) {
        throw new JamelinRuntimeException(errMsg);
    }
}
