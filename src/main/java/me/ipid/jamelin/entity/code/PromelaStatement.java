package me.ipid.jamelin.entity.code;

import me.ipid.jamelin.entity.ProcessInfo;
import me.ipid.jamelin.execute.JamelinKernel;

public interface PromelaStatement {
    void execute(JamelinKernel kernel, ProcessInfo procInfo);
}
