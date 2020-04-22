package me.ipid.jamelin.entity.sa;

import lombok.Getter;

import java.util.Objects;

public class SAPrimitiveType implements SAPromelaType {

    // 原始类型的名字（如：int）
    @Getter
    private final String name;

    // 是否有符号
    public final boolean signed;

    // 变量数据长度（单位：bits）
    public final int bitLen;

    @Getter
    public final int typeId;

    public SAPrimitiveType(String name, boolean signed, int bitLen, int typeId) {
        this.name = name;
        this.signed = signed;
        this.bitLen = bitLen;
        this.typeId = typeId;
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    public boolean isPrimitiveArray() {
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, signed, bitLen);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SAPrimitiveType)) {
            return false;
        }

        var other = (SAPrimitiveType) obj;
        return name.equals(other.name) && signed == other.signed && bitLen == other.bitLen;
    }
}
