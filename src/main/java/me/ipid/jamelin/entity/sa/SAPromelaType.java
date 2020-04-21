package me.ipid.jamelin.entity.sa;

import me.ipid.jamelin.entity.il.ILNamedItem;

import java.util.ArrayList;

public interface SAPromelaType extends ILNamedItem {
    int getSize();

    int getTypeId();

    boolean isPrimitiveArray();
}
