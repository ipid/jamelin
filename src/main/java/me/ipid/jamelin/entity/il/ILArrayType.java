package me.ipid.jamelin.entity.il;

import lombok.Data;
import me.ipid.jamelin.entity.MemorySlot;

import java.util.List;

public @Data
class ILArrayType implements ILType {
    public final int arrayLen;
    public final ILType target;

    @Override
    public int getSize() {
        return arrayLen * target.getSize();
    }

    @Override
    public void fillMemoryLayout(List<MemorySlot> container) {
        for (int i = 0; i < arrayLen; i++) {
            target.fillMemoryLayout(container);
        }
    }
}
