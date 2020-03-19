package me.ipid.jamelin.entity;

import me.ipid.jamelin.entity.symbol.*;

import java.util.ArrayList;
import java.util.List;

public class RuntimeInfo {
    private Proctype initProc;
    private List<MemorySlot> globalMemoryLayout;

    public RuntimeInfo() {
        this.initProc = new Proctype(-1);
        this.globalMemoryLayout = new ArrayList<>();
    }

    public Proctype getInitProc() {
        return initProc;
    }

    public RuntimeInfo setInitProc(Proctype initProc) {
        this.initProc = initProc;
        return this;
    }

    public List<MemorySlot> getGlobalMemoryLayout() {
        return globalMemoryLayout;
    }

    public RuntimeInfo setGlobalMemoryLayout(List<MemorySlot> globalMemoryLayout) {
        this.globalMemoryLayout = globalMemoryLayout;
        return this;
    }
}
