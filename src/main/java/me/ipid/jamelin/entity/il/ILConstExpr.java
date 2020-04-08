package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.entity.*;
import me.ipid.jamelin.execute.*;

public class ILConstExpr implements ILExpr {

    private int num;

    public ILConstExpr(int num) {
        this.num = num;
    }

    @Override
    public int execute(JamelinKernel kernel, ProcessControlBlock procInfo) {
        return num;
    }

}
