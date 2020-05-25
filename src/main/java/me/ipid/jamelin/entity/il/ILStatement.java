package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.execute.Kernel;
import me.ipid.jamelin.execute.ProcessControlBlock;

public interface ILStatement {
    void execute(Kernel kernel, ProcessControlBlock procInfo);
}
