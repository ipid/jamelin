package me.ipid.jamelin.entity.state;

import me.ipid.jamelin.entity.il.ILConstExpr;
import me.ipid.jamelin.entity.il.ILExpr;
import me.ipid.jamelin.entity.il.ILStatement;

import java.util.ArrayList;
import java.util.List;

public class TransitionEdge implements Comparable<TransitionEdge> {
    public static final int DUMMY_PRIORITY = 0;

    private int priority;
    private List<ILStatement> action;
    private ILExpr condition;
    private StateNode to;

    public TransitionEdge(StateNode to) {
        this(to, new ILConstExpr(1));
    }

    public TransitionEdge(StateNode to, ILExpr cond) {
        this.priority = DUMMY_PRIORITY;
        this.action = new ArrayList<>();
        this.condition = cond;
        this.to = to;
    }

    public List<ILStatement> getAction() {
        return action;
    }

    public TransitionEdge setAction(List<ILStatement> action) {
        this.action = action;
        return this;
    }

    public ILExpr getCondition() {
        return condition;
    }

    public TransitionEdge setCondition(ILExpr condition) {
        this.condition = condition;
        return this;
    }

    public int getPriority() {
        return priority;
    }

    public TransitionEdge setPriority(int priority) {
        this.priority = priority;
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
