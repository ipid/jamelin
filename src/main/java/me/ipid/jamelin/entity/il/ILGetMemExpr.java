package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.entity.*;
import me.ipid.jamelin.execute.*;

public class ILGetMemExpr implements ILExpr {
    private boolean global;
    private int memOffset;

    public ILGetMemExpr(boolean global, int memOffset) {
        this.global = global;
        this.memOffset = memOffset;
    }

    public boolean isGlobal() {
        return global;
    }

    public int getMemOffset() {
        return memOffset;
    }

    @Override
    public int execute(JamelinKernel kernel, ProcessControlBlock procInfo) {
        if (global) {
            return kernel.getGlobalSlot(memOffset).getValue();
        }

        return procInfo.getProceesSlot(memOffset).getValue();
    }
}
