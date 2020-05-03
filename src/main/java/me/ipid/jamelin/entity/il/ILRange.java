package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.execute.JamelinKernel;
import me.ipid.jamelin.execute.ProcessControlBlock;
import me.ipid.util.tupling.Tuple2;

public class ILRange {
    public final ILExpr startIn, endEx;
    public final boolean global;

    public ILRange(ILExpr startIn, ILExpr endEx, boolean global) {
        this.startIn = startIn;
        this.endEx = endEx;
        this.global = global;
    }

    public Tuple2<Integer, Integer> execute(JamelinKernel kernel, ProcessControlBlock procInfo) {
        return Tuple2.of(
                startIn.execute(kernel, procInfo, false),
                endEx.execute(kernel, procInfo, false));
    }
}
