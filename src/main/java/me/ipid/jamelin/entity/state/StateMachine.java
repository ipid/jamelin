package me.ipid.jamelin.entity.state;

public class StateMachine {
    private StateNode start, end;

    public StateMachine() {
        start = end = new StateNode();
    }

    public StateNode getEnd() {
        return end;
    }

    public StateMachine setEnd(StateNode end) {
        this.end = end;
        return this;
    }

    public StateNode getStart() {
        return start;
    }

    public StateMachine setStart(StateNode start) {
        this.start = start;
        return this;
    }
}
