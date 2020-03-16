package me.ipid.jamelin.entity.code;

import me.ipid.jamelin.entity.ProcessInfo;
import me.ipid.jamelin.execute.JamelinKernel;

public interface PromelaExpr {
    int execute(JamelinKernel kernel, ProcessInfo procInfo);
}
