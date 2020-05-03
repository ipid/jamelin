package me.ipid.jamelin.entity.sa;

import me.ipid.jamelin.entity.il.ILNamedItem;
import me.ipid.jamelin.util.Slot;

import java.util.List;

public interface SAPromelaType extends ILNamedItem {
    int getSize();
    int getTypeId();
    boolean isPrimitiveArray();

    /**
     * 将此类型的每一个槽放入 slots 容器中。
     * 槽对应 0 - (size-1) 的元素，表示该元素的类型。
     *
     * @param slots 槽容器，用于放置新的槽对象
     */
    void fillSlots(List<? super Slot> slots);
}
