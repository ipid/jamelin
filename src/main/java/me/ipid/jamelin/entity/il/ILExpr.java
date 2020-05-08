package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.execute.JamelinKernel;
import me.ipid.jamelin.execute.ProcessControlBlock;

public interface ILExpr {
    /**
     * 执行该表达式，可能有副作用。
     *
     * @return 表达式的值，非 0 表示 true
     */
    int execute(JamelinKernel kernel, ProcessControlBlock procInfo, boolean noSideEffect);
}
