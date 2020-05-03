package me.ipid.jamelin.entity.state;

import me.ipid.jamelin.entity.il.ILConstExpr;
import me.ipid.jamelin.entity.il.ILExpr;
import me.ipid.jamelin.entity.il.ILStatement;

import java.util.ArrayList;
import java.util.List;

public class TransitionEdge implements Comparable<TransitionEdge> {
    public static final int DUMMY_PRIORITY = 0;

    public final int priority;
    public final List<ILStatement> action;
    public final ILExpr condition;
    public final StateNode to;

    public TransitionEdge(StateNode to) {
        this(to, new ILConstExpr(1));
    }

    public TransitionEdge(StateNode to, ILExpr cond) {
        this.priority = DUMMY_PRIORITY;
        this.action = new ArrayList<>();
        this.condition = cond;
        this.to = to;
    }

    @Override
    public int compareTo(TransitionEdge o) {
        return this.priority - o.priority;
    }
}
