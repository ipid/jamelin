package me.ipid.jamelin.entity.statement;

import me.ipid.jamelin.entity.ProcessControlBlock;
import me.ipid.jamelin.execute.JamelinKernel;

public interface PromelaStatement {
    void execute(JamelinKernel kernel, ProcessControlBlock procInfo);
}
