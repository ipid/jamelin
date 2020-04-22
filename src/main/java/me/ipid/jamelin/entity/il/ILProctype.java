package me.ipid.jamelin.entity.il;

import lombok.Getter;
import me.ipid.jamelin.entity.state.StateMachine;

import java.util.ArrayList;
import java.util.List;

public class ILProctype implements ILNamedItem {
    @Getter
    public final String name;

    public final StateMachine stateMachine;
    public final List<Integer> memory;

    public ILProctype(String name) {
        this.name = name;
        this.stateMachine = new StateMachine();
        this.memory = new ArrayList<>();
    }
}
