package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.entity.*;
import me.ipid.jamelin.execute.*;

public interface ILStatement {
    void execute(JamelinKernel kernel, ProcessControlBlock procInfo);
}
