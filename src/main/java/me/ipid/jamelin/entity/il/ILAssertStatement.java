package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.entity.ProcessControlBlock;
import me.ipid.jamelin.exception.RuntimeExceptions.JamelinRuntimeException;
import me.ipid.jamelin.execute.JamelinKernel;

public class ILAssertStatement implements ILStatement {
    public final ILExpr shouldTrue;
    public final String errMsg;

    public ILAssertStatement(ILExpr shouldTrue, String errMsg) {
        this.shouldTrue = shouldTrue;
        this.errMsg = errMsg;
    }

    @Override
    public void execute(JamelinKernel kernel, ProcessControlBlock procInfo) {
        if (shouldTrue.execute(kernel, procInfo) == 0) {
            throw new JamelinRuntimeException(errMsg);
        }
    }
}
