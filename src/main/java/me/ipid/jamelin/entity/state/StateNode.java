package me.ipid.jamelin.entity.state;

import java.util.ArrayList;
import java.util.List;

public class StateNode {
    private List<TransitionEdge> outEdge;

    public StateNode() {
        outEdge = new ArrayList<>();
    }

    public List<TransitionEdge> getOutEdge() {
        return outEdge;
    }

    public StateNode setOutEdge(List<TransitionEdge> outEdge) {
        this.outEdge = outEdge;
        return this;
    }
}
