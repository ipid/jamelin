package me.ipid.jamelin.execute;

import me.ipid.jamelin.entity.state.StateNode;

import java.util.ArrayList;
import java.util.List;

public class ProcessControlBlock {

    public final String name;
    public final ArrayList<Integer> memorySlots;
    private int pid;
    private StateNode currState;
    public final StateNode end;
    private boolean currElse;

    public ProcessControlBlock(
            String name, int pid, List<Integer> memoryTemplate,
            StateNode initialState, StateNode end) {
        this.name = name;
        this.pid = pid;
        this.memorySlots = new ArrayList<>(memoryTemplate);

        this.currState = initialState;
        this.end = end;
    }

    public StateNode getCurrState() {
        return currState;
    }

    public ProcessControlBlock setCurrState(StateNode currState) {
        this.currState = currState;
        return this;
    }

    public int getPid() {
        return pid;
    }

    public ProcessControlBlock setPid(int pid) {
        this.pid = pid;
        return this;
    }

    public boolean isElse() {
        return currElse;
    }

    public ProcessControlBlock setElse(boolean currElse) {
        this.currElse = currElse;
        return this;
    }

    public int getProcessMemory(int offset) {
        return memorySlots.get(offset);
    }

    public void setProcessMemory(int offset, int value) {
        memorySlots.set(offset, value);
    }
}
