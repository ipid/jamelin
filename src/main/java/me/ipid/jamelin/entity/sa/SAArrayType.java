package me.ipid.jamelin.entity.sa;

import lombok.Getter;
import me.ipid.jamelin.util.Slot;

import java.util.List;
import java.util.Objects;

public class SAArrayType implements SAPromelaType {

    public final SAPromelaType type;
    public final int arrLen;

    @Getter
    private final String name;

    public SAArrayType(SAPromelaType type, int arrLen) {
        this.type = type;
        this.arrLen = arrLen;
        this.name = type.getName() + '[' + arrLen + ']';
    }

    @Override
    public int getSize() {
        return arrLen * type.getSize();
    }

    @Override
    public int getTypeId() {
        return type.getTypeId();
    }

    @Override
    public boolean isPrimitiveArray() {
        return type instanceof SAPrimitiveType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, arrLen);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SAArrayType)) {
            return false;
        }
        var other = (SAArrayType) obj;
        return type.equals(other.type) && arrLen == other.arrLen;
    }

    @Override
    public void fillSlots(List<? super Slot> slots) {
        for (int i = 0; i < arrLen; i++) {
            type.fillSlots(slots);
        }
    }
}
