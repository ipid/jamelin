package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.entity.ProcessControlBlock;
import me.ipid.jamelin.execute.JamelinKernel;

public class ILGetDynMemExpr implements ILExpr {
    public final boolean global;
    public final ILExpr memOffset;

    public ILGetDynMemExpr(boolean global, ILExpr memOffset) {
        this.global = global;
        this.memOffset = memOffset;
    }

    @Override
    public int execute(JamelinKernel kernel, ProcessControlBlock procInfo) {
        int offset = memOffset.execute(kernel, procInfo);

        if (global) {
            return kernel.getGlobalMemory(offset);
        }

        return procInfo.getProcessMemory(offset);
    }
}
