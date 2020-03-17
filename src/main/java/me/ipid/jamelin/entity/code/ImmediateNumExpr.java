package me.ipid.jamelin.entity.code;

import me.ipid.jamelin.entity.ProcessControlBlock;
import me.ipid.jamelin.execute.JamelinKernel;

public class ImmediateNumExpr implements PromelaExpr {

    private int num;

    public ImmediateNumExpr(int num) {
        this.num = num;
    }

    @Override
    public int execute(JamelinKernel kernel, ProcessControlBlock procInfo) {
        return num;
    }

}
