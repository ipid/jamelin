package me.ipid.jamelin.entity.state;

import me.ipid.jamelin.entity.il.ILStatement;

import java.util.List;

public class StateMachine {
    private StateNode start, end;

    public StateMachine() {
        start = end = new StateNode();
    }

    public StateNode getStart() {
        return start;
    }

    public StateMachine setStart(StateNode start) {
        this.start = start;
        return this;
    }

    public StateNode getEnd() {
        return end;
    }

    public StateMachine setEnd(StateNode end) {
        this.end = end;
        return this;
    }

    public StateMachine link(StateNode oldNode, StateNode newNode, List<ILStatement> statements) {
        TransitionEdge edge = new TransitionEdge(0, newNode);
        edge.getAction().addAll(statements);

        oldNode.getOutEdge().add(edge);
        return this;
    }

    public StateMachine addEdge(StateNode node, TransitionEdge edge) {
        node.getOutEdge().add(edge);
        return this;
    }

    public StateMachine linkToNewEnd(List<ILStatement> statements) {
        StateNode newNode = new StateNode();

        link(end, newNode, statements);
        end = newNode;

        return this;
    }
}
