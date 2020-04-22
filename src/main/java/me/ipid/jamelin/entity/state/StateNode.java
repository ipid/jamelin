package me.ipid.jamelin.entity.state;

import java.util.ArrayList;
import java.util.List;

public class StateNode {
    public final List<TransitionEdge> outEdge;

    public StateNode() {
        outEdge = new ArrayList<>();
    }
}
