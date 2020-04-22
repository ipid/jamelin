package me.ipid.jamelin.entity.sa;

import lombok.Getter;

public class SAUtype implements SAPromelaType {
    public final SASymbolTable fields;

    @Getter
    public final int typeId;

    public SAUtype(int typeId) {
        this.fields = new SASymbolTable(false);
        this.typeId = typeId;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public int getSize() {
        return fields.getGlobalLen();
    }

    @Override
    public boolean isPrimitiveArray() {
        return false;
    }

    @Override
    public int hashCode() {
        return typeId;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SAUtype)) {
            return false;
        }
        var other = (SAUtype) obj;
        return typeId == other.typeId;
    }
}
