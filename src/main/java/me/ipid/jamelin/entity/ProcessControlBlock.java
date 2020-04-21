package me.ipid.jamelin.entity;

import me.ipid.jamelin.entity.state.*;
import me.ipid.jamelin.util.*;

import java.util.ArrayList;
import java.util.List;

public class ProcessControlBlock {

    public final ArrayList<Integer> memorySlots;
    private int pid;
    private StateNode currState;

    public ProcessControlBlock(int pid, List<Integer> memoryTemplate, StateNode initialState) {
        this.pid = pid;
        this.memorySlots = new ArrayList<>(memoryTemplate);

        this.currState = initialState;
    }

    public int getPid() {
        return pid;
    }

    public ProcessControlBlock setPid(int pid) {
        this.pid = pid;
        return this;
    }

    public StateNode getCurrState() {
        return currState;
    }

    public ProcessControlBlock setCurrState(StateNode currState) {
        this.currState = currState;
        return this;
    }

    public int getProcessMemory(int offset) {
        return memorySlots.get(offset);
    }

    public void setProcessMemory(int offset, int value) {
        memorySlots.set(offset, value);
    }
}
