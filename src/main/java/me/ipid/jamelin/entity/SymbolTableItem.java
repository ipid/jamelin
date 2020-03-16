package me.ipid.jamelin.entity;

public class SymbolTableItem {

    private PromelaType type;
    private int startAddr;

    public SymbolTableItem(PromelaType type, int startAddr) {
        this.type = type;
        this.startAddr = startAddr;
    }

    public PromelaType getType() {
        return type;
    }

    public int getStartAddr() {
        return startAddr;
    }
}

