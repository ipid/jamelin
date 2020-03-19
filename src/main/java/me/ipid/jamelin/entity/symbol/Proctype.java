package me.ipid.jamelin.entity.symbol;

import me.ipid.jamelin.entity.*;
import me.ipid.jamelin.entity.state.*;

import java.util.ArrayList;
import java.util.List;

public class Proctype implements PromelaNamedItem {
    private StateMachine stateMachine;
    private int id;
    private List<MemorySlot> memoryLayout;

    public Proctype(int id) {
        this.stateMachine = new StateMachine();
        this.id = id;
        this.memoryLayout = new ArrayList<>();
    }

    public StateMachine getStateMachine() {
        return stateMachine;
    }

    public Proctype setStateMachine(StateMachine stateMachine) {
        this.stateMachine = stateMachine;
        return this;
    }

    public int getId() {
        return id;
    }

    public Proctype setId(int id) {
        this.id = id;
        return this;
    }

    public List<MemorySlot> getMemoryLayout() {
        return memoryLayout;
    }

    public Proctype setMemoryLayout(List<MemorySlot> memoryLayout) {
        this.memoryLayout = memoryLayout;
        return this;
    }
}
