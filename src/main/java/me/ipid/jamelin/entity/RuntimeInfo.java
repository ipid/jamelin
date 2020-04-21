package me.ipid.jamelin.entity;

import me.ipid.jamelin.entity.il.ILProctype;
import me.ipid.jamelin.entity.il.ILStatement;
import me.ipid.util.lateinit.LateInit;

import java.util.ArrayList;
import java.util.List;

public class RuntimeInfo {
    public final List<ILProctype> procs, activeProcs;

    // 内存初始值
    public final List<Integer> globalMemory;

    // 初始化语句（给全局变量赋予初值）
    public final List<ILStatement> initStatements;

    public RuntimeInfo() {
        this.procs = new ArrayList<>();
        this.activeProcs = new ArrayList<>();
        this.globalMemory = new ArrayList<>();
        this.initStatements = new ArrayList<>();
    }
}
