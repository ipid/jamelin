package me.ipid.jamelin.entity.state;

import me.ipid.jamelin.entity.il.ILExpr;
import me.ipid.jamelin.entity.il.ILStatement;

import java.util.List;

public class StateUtil {
    public static void link(StateNode start, StateNode end, List<ILStatement> statements) {
        var edge = new TransitionEdge(end);
        edge.action.addAll(statements);
        start.outEdge.add(edge);
    }

    public static void linkSingle(StateNode start, StateNode end, ILStatement statement) {
        var edge = new TransitionEdge(end);
        edge.action.add(statement);
        start.outEdge.add(edge);
    }

    public static void linkBlocking(StateNode start, StateNode end, ILStatement statement, ILExpr expr) {
        var edge = new TransitionEdge(end, expr);
        edge.action.add(statement);
        start.outEdge.add(edge);
    }

    public static void linkWithCond(StateNode start, StateNode end, ILExpr cond) {
        var edge = new TransitionEdge(end, cond);
        start.outEdge.add(edge);
    }
}
