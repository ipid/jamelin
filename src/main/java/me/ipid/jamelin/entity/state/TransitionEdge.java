package me.ipid.jamelin.entity.state;

import me.ipid.jamelin.entity.expr.*;
import me.ipid.jamelin.entity.statement.*;

import java.util.ArrayList;
import java.util.List;

public class TransitionEdge implements Comparable<TransitionEdge> {
    private int priority;
    private List<PromelaStatement> action;
    private PromelaExpr condition;
    private StateNode to;

    public TransitionEdge(int priority, StateNode to) {
        this.priority = priority;
        this.action = new ArrayList<>();
        this.condition = new ImmediateNumExpr(1);
        this.to = to;
    }

    public int getPriority() {
        return priority;
    }

    public TransitionEdge setPriority(int priority) {
        this.priority = priority;
        return this;
    }

    public List<PromelaStatement> getAction() {
        return action;
    }

    public TransitionEdge setAction(List<PromelaStatement> action) {
        this.action = action;
        return this;
    }

    public PromelaExpr getCondition() {
        return condition;
    }

    public TransitionEdge setCondition(PromelaExpr condition) {
        this.condition = condition;
        return this;
    }

    public StateNode getTo() {
        return to;
    }

    public TransitionEdge setTo(StateNode to) {
        this.to = to;
        return this;
    }

    @Override
    public int compareTo(TransitionEdge o) {
        return this.priority - o.priority;
    }
}
