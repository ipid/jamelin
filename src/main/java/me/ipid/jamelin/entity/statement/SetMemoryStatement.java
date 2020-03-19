package me.ipid.jamelin.entity.statement;

import me.ipid.jamelin.entity.*;
import me.ipid.jamelin.entity.expr.*;
import me.ipid.jamelin.execute.*;

public class SetMemoryStatement implements PromelaStatement {

    private boolean global;
    private int offset;
    private PromelaExpr valueExpr;

    public SetMemoryStatement(boolean global, int offset, PromelaExpr valueExpr) {
        this.global = global;
        this.offset = offset;
        this.valueExpr = valueExpr;
    }

    public boolean isGlobal() {
        return global;
    }

    public int getOffset() {
        return offset;
    }

    public PromelaExpr getValueExpr() {
        return valueExpr;
    }

    @Override
    public void execute(JamelinKernel kernel, ProcessControlBlock procInfo) {
        int value = valueExpr.execute(kernel, procInfo);

        if (global) {
            kernel.getGlobalSlot(offset).setValue(value);
        } else {
            procInfo.getProceesSlot(offset).setValue(value);
        }
    }
}
