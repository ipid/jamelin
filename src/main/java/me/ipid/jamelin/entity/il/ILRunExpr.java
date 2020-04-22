package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.execute.JamelinKernel;
import me.ipid.jamelin.execute.ProcessControlBlock;

public class ILRunExpr implements ILExpr {
    public final int serialNum;

    public ILRunExpr(int serialNum) {
        this.serialNum = serialNum;
    }

    @Override
    public int execute(JamelinKernel kernel, ProcessControlBlock procInfo) {
        return kernel.createProcess(serialNum);
    }
}
