package me.ipid.jamelin.entity.expr;

import me.ipid.jamelin.entity.*;
import me.ipid.jamelin.execute.*;

public interface PromelaExpr {
    int execute(JamelinKernel kernel, ProcessControlBlock procInfo);
}
