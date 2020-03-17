package me.ipid.jamelin.entity.symbol;

import me.ipid.jamelin.entity.state.StateMachine;

public class Proctype implements PromelaNamedItem {
    private StateMachine stateMachine;

    public Proctype() {
        stateMachine = new StateMachine();
    }

    public StateMachine getStateMachine() {
        return stateMachine;
    }

    public Proctype setStateMachine(StateMachine stateMachine) {
        this.stateMachine = stateMachine;
        return this;
    }
}
