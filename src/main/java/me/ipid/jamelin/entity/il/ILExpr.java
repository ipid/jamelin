package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.entity.*;
import me.ipid.jamelin.execute.*;

public interface ILExpr {
    int execute(JamelinKernel kernel, ProcessControlBlock procInfo);
}
