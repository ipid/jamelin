package me.ipid.jamelin.entity.il;

import lombok.Getter;
import me.ipid.jamelin.entity.*;
import me.ipid.jamelin.entity.state.*;

import java.util.ArrayList;
import java.util.List;

public class ILProctype implements ILNamedItem {
    @Getter
    public final String name;

    public final StateMachine stateMachine;
    public final List<MemorySlot> memoryLayout;

    public ILProctype(String name) {
        this.name = name;
        this.stateMachine = new StateMachine();
        this.memoryLayout = new ArrayList<>();
    }
}
