package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.execute.JamelinKernel;
import me.ipid.jamelin.execute.ProcessControlBlock;

public class ILConstExpr implements ILExpr {

    public final int num;

    public ILConstExpr(int num) {
        this.num = num;
    }

    @Override
    public int execute(JamelinKernel kernel, ProcessControlBlock procInfo, boolean noSideEffect) {
        return num;
    }
}
