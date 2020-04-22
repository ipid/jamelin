package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.execute.JamelinKernel;
import me.ipid.jamelin.execute.ProcessControlBlock;

public class ILGetMemExpr implements ILExpr {
    private boolean global;
    private int memOffset;

    public ILGetMemExpr(boolean global, int memOffset) {
        this.global = global;
        this.memOffset = memOffset;
    }

    public int getMemOffset() {
        return memOffset;
    }

    public boolean isGlobal() {
        return global;
    }

    @Override
    public int execute(JamelinKernel kernel, ProcessControlBlock procInfo) {
        if (global) {
            return kernel.getGlobalMemory(memOffset);
        }

        return procInfo.getProcessMemory(memOffset);
    }
}
