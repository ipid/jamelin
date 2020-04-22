package me.ipid.jamelin.entity.sa;

import me.ipid.jamelin.entity.il.ILNamedItem;

public interface SAPromelaType extends ILNamedItem {
    int getSize();

    int getTypeId();

    boolean isPrimitiveArray();
}
