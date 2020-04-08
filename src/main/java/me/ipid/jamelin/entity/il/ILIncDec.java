package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.entity.*;
import me.ipid.jamelin.execute.*;

public class ILIncDec implements ILStatement {

    private boolean global;
    private int offset;
    private boolean increment;

    public ILIncDec(boolean global, int offset, boolean increment) {
        this.global = global;
        this.offset = offset;
        this.increment = increment;
    }

    public boolean isGlobal() {
        return global;
    }

    public int getOffset() {
        return offset;
    }

    public boolean isIncrement() {
        return increment;
    }

    @Override
    public void execute(JamelinKernel kernel, ProcessControlBlock procInfo) {
        MemorySlot slot;
        if (global) {
            slot = kernel.getGlobalSlot(offset);
        } else {
            slot = procInfo.getProceesSlot(offset);
        }

        int newValue = slot.getValue() + (increment ? 1 : -1);
        slot.setValue(newValue);
    }
}
