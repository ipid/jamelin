package me.ipid.jamelin.entity.sa;

import lombok.Getter;
import me.ipid.jamelin.util.Slot;

import java.util.List;

public class SAUtype implements SAPromelaType {
    public final SASymbolTable fields;

    @Getter
    public final int typeId;

    @Getter
    public final String name;

    public SAUtype(String name, int typeId) {
        this.fields = new SASymbolTable(false);
        this.typeId = typeId;
        this.name = name;
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
    public void fillSlots(List<? super Slot> slots) {
        fields.fillSlots(slots);
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
