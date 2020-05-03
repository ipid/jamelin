package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.execute.JamelinKernel;
import me.ipid.jamelin.execute.ProcessControlBlock;

public class ILGetDynMemExpr implements ILExpr {
    public final boolean global;
    public final ILExpr memOffset;

    public ILGetDynMemExpr(boolean global, ILExpr memOffset) {
        this.global = global;
        this.memOffset = memOffset;
    }

    @Override
    public int execute(JamelinKernel kernel, ProcessControlBlock procInfo, boolean noSideEffect) {
        int offset = memOffset.execute(kernel, procInfo, noSideEffect);

        if (global) {
            return kernel.getGlobalMemory(offset);
        }

        return procInfo.getProcessMemory(offset);
    }
}
