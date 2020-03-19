package me.ipid.jamelin.entity.symbol;

public class SymbolTableItem implements PromelaNamedItem {

    private PromelaType type;
    private int startAddr;
    private boolean isGlobal;

    public SymbolTableItem(PromelaType type, int startAddr, boolean isGlobal) {
        this.type = type;
        this.startAddr = startAddr;
        this.isGlobal = isGlobal;
    }

    public PromelaType getType() {
        return type;
    }

    public int getStartAddr() {
        return startAddr;
    }

    public boolean isGlobal() {
        return isGlobal;
    }
}

