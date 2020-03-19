package me.ipid.jamelin.entity;

import me.ipid.jamelin.entity.state.*;
import me.ipid.jamelin.util.*;

import java.util.ArrayList;
import java.util.List;

public class ProcessControlBlock {

    private int pid;
    private List<MemorySlot> memorySlots;

    private StateNode currState;

    public ProcessControlBlock(int pid, List<MemorySlot> slotsTemplate, StateNode initialState) {
        this.pid = pid;

        this.memorySlots = new ArrayList<>();
        MemoryUtil.copyMemorySlots(slotsTemplate, this.memorySlots);

        this.currState = initialState;
    }

    public int getPid() {
        return pid;
    }

    public ProcessControlBlock setPid(int pid) {
        this.pid = pid;
        return this;
    }

    public List<MemorySlot> getMemorySlots() {
        return memorySlots;
    }

    public ProcessControlBlock setMemorySlots(List<MemorySlot> memorySlots) {
        this.memorySlots = memorySlots;
        return this;
    }

    public StateNode getCurrState() {
        return currState;
    }

    public ProcessControlBlock setCurrState(StateNode currState) {
        this.currState = currState;
        return this;
    }

    public MemorySlot getProceesSlot(int i) {
        return memorySlots.get(i);
    }

}
