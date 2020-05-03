package me.ipid.jamelin.entity;

import lombok.NonNull;
import me.ipid.jamelin.entity.sa.SAPromelaType;
import me.ipid.jamelin.entity.sa.SASymbolTable;
import me.ipid.jamelin.entity.state.StateNode;

import java.util.ArrayDeque;
import java.util.Map;

public class CompileTimeInfo {
    public final SASymbolTable table;
    public final NamedItemContainer nItems;
    public final ArrayDeque<StateNode> loopExit;

    public CompileTimeInfo(@NonNull Map<String, SAPromelaType> initialTypes) {
        this.table = new SASymbolTable();
        this.nItems = new NamedItemContainer(initialTypes);
        this.loopExit = new ArrayDeque<>();
    }
}
