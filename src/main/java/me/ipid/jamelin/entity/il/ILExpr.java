package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.execute.JamelinKernel;
import me.ipid.jamelin.execute.ProcessControlBlock;

public interface ILExpr {
    /**
     * 执行该表达式，可能有副作用。
     * @return 表达式的值，非 0 表示 true
     */
    int execute(JamelinKernel kernel, ProcessControlBlock procInfo);

    /**
     * 测试该表达式是否表示 true。无副作用。
     * @return 若表达式当前为 true，则返回 true
     */
    default boolean checkCond(JamelinKernel kernel, ProcessControlBlock procInfo) {
        return execute(kernel, procInfo) != 0;
    }
}
