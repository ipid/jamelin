package me.ipid.jamelin.entity.code;

import me.ipid.jamelin.entity.ProcessControlBlock;
import me.ipid.jamelin.execute.JamelinKernel;

public interface PromelaExpr {
    int execute(JamelinKernel kernel, ProcessControlBlock procInfo);
}
