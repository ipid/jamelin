package me.ipid.jamelin.entity.il;

import lombok.Data;
import lombok.Getter;
import me.ipid.jamelin.entity.MemorySlot;

import java.util.List;


public @Data
class ILSimpleType implements ILType, ILNamedItem {
    // 是否有符号
    public final boolean signed;

    // 变量数据长度（单位：bits）、初始值
    public final int bitLen, initialValue;

    @Getter
    public final String name;

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    public void fillMemoryLayout(List<MemorySlot> container) {
        container.add(new MemorySlot(signed, bitLen, initialValue));
    }
}
