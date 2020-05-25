package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.execute.Kernel;
import me.ipid.jamelin.execute.ProcessControlBlock;

public class ILRunExpr implements ILExpr {
    public final int serialNum;

    public ILRunExpr(int serialNum) {
        this.serialNum = serialNum;
    }

    @Override
    public int execute(Kernel kernel, ProcessControlBlock procInfo, boolean noSideEffect) {
        // 当没有副作用时，表达式可能被当做转移边的条件
        // 由于 run 语句在语义上不阻塞，因此总是返回 true
        if (noSideEffect) {
            return 1;
        }

        return kernel.createProcess(serialNum);
    }
}
