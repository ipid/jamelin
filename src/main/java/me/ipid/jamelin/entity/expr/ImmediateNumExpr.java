package me.ipid.jamelin.entity.expr;

import me.ipid.jamelin.entity.*;
import me.ipid.jamelin.execute.*;

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
