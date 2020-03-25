package me.ipid.jamelin.entity.symbol;

import me.ipid.jamelin.entity.*;
import me.ipid.jamelin.entity.state.*;

import java.util.ArrayList;
import java.util.List;

public class Proctype implements PromelaNamedItem {
    public final StateMachine stateMachine;
    public final int id;
    public final List<MemorySlot> memoryLayout;

    public Proctype(int id) {
        this.stateMachine = new StateMachine();
        this.id = id;
        this.memoryLayout = new ArrayList<>();
    }
}
