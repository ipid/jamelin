package me.ipid.jamelin.entity;

import me.ipid.jamelin.entity.state.*;
import me.ipid.jamelin.entity.symbol.*;
import me.ipid.jamelin.util.*;

import java.util.HashMap;
import java.util.Map;

public class CompileTimeInfo {
    private SymbolTable table;
    private Map<String, PromelaNamedItem> entities;
    private NameIndexer proctypeIndex;
    private String procName;

    public CompileTimeInfo() {
        table = new SymbolTable();
        entities = new HashMap<>();
        proctypeIndex = new NameIndexer();
        procName = "init";
    }

    public SymbolTable getTable() {
        return table;
    }

    public CompileTimeInfo setTable(SymbolTable table) {
        this.table = table;
        return this;
    }

    public Map<String, PromelaNamedItem> getEntities() {
        return entities;
    }

    public CompileTimeInfo setEntities(Map<String, PromelaNamedItem> entities) {
        this.entities = entities;
        return this;
    }

    public NameIndexer getProctypeIndex() {
        return proctypeIndex;
    }

    public CompileTimeInfo setProctypeIndex(NameIndexer proctypeIndex) {
        this.proctypeIndex = proctypeIndex;
        return this;
    }

    public String getProcName() {
        return procName;
    }

    public CompileTimeInfo setProcName(String procName) {
        if (entities.get(procName) == null || !(entities.get(procName) instanceof Proctype)) {
            throw new Error(String.format("当前实体表中未找到 %s 进程", procName));
        }

        this.procName = procName;
        return this;
    }

    /* API */

    public StateMachine getCurrMachine() {
        return getProc().getStateMachine();
    }

    public Proctype getProc() {
        return (Proctype) entities.get(procName);
    }
}

