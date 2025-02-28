package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.execute.Kernel;
import me.ipid.jamelin.execute.ProcessControlBlock;

public class ILGetMemExpr implements ILExpr {
    public final boolean global;
    public final ILExpr memOffset;

    public ILGetMemExpr(boolean global, ILExpr memOffset) {
        this.global = global;
        this.memOffset = memOffset;
    }

    @Override
    public int execute(Kernel kernel, ProcessControlBlock procInfo, boolean noSideEffect) {
        int offset = memOffset.execute(kernel, procInfo, noSideEffect);

        if (global) {
            return kernel.getGlobalMemory(offset);
        }

        return procInfo.getProcessMemory(offset);
    }
}
