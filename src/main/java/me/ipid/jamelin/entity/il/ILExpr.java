package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.execute.JamelinKernel;
import me.ipid.jamelin.execute.ProcessControlBlock;

public interface ILExpr {
    int execute(JamelinKernel kernel, ProcessControlBlock procInfo);
}
