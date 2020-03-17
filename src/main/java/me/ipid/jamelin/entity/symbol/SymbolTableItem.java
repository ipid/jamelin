package me.ipid.jamelin.entity.symbol;

public class SymbolTableItem implements PromelaNamedItem {

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

