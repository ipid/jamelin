package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.constant.PromelaLanguage.PredefVar;
import me.ipid.jamelin.entity.ProcessControlBlock;
import me.ipid.jamelin.execute.JamelinKernel;

public class ILPredefExpr implements ILExpr {

    public final PredefVar pre;

    public ILPredefExpr(PredefVar pre) {
        this.pre = pre;
    }

    @Override
    public int execute(JamelinKernel kernel, ProcessControlBlock procInfo) {
        throw new Error("TODO");
    }
}
