package me.ipid.jamelin.entity.il;

import lombok.Getter;
import me.ipid.jamelin.entity.state.StateMachine;
import me.ipid.jamelin.entity.state.StateNode;

import java.util.ArrayList;
import java.util.List;

public final class ILProctype implements ILNamedItem {
    @Getter
    public final String name;

    public final StateMachine machine;
    public final List<Integer> memory;

    public ILProctype(String name) {
        this.name = name;
        this.machine = new StateMachine();
        this.memory = new ArrayList<>();
    }

    public StateNode getEnd() {
        return machine.getEnd();
    }

    public StateNode getStart() {
        return machine.getStart();
    }
}
