package me.ipid.jamelin.entity.statement;

import me.ipid.jamelin.entity.*;
import me.ipid.jamelin.execute.*;

public interface PromelaStatement {
    void execute(JamelinKernel kernel, ProcessControlBlock procInfo);
}
