package me.ipid.jamelin.entity;

import me.ipid.jamelin.entity.il.ILProctype;

import java.util.ArrayList;
import java.util.List;

public class RuntimeInfo {
    public final List<ILProctype> procs, activeProcs;
    public final List<MemorySlot> globalMemoryLayout;

    public RuntimeInfo() {
        this.procs = new ArrayList<>();
        this.activeProcs = new ArrayList<>();
        this.globalMemoryLayout = new ArrayList<>();
    }
}
