package me.ipid.jamelin.entity.expr;

import me.ipid.jamelin.entity.*;
import me.ipid.jamelin.execute.*;

public class GetMemExpr implements PromelaExpr {
    private boolean global;
    private int memOffset;

    public GetMemExpr(boolean global, int memOffset) {
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
