package me.ipid.jamelin.entity.sa;

import lombok.Getter;

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
    public boolean isPrimitiveArray() {
        return type instanceof SAPrimitiveType;
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
}
