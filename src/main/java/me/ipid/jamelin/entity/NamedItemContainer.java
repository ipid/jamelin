package me.ipid.jamelin.entity;

import me.ipid.jamelin.entity.il.ILNamedItem;
import me.ipid.jamelin.entity.il.ILProctype;
import me.ipid.jamelin.entity.sa.SAPromelaType;
import org.apache.commons.collections4.map.ListOrderedMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class NamedItemContainer {
    private final Map<String, ILNamedItem> namedItems;
    public final ListOrderedMap<String, ILProctype> proctypes;
    public final Map<String, SAPromelaType> types;

    public NamedItemContainer(Map<String, SAPromelaType> initialTypes) {
        namedItems = new HashMap<>();
        proctypes = new ListOrderedMap<>();
        types = initialTypes;
    }

    public Optional<ILNamedItem> getItem(String name) {
        return Optional.ofNullable(namedItems.get(name));
    }

    public void putProctype(String name, ILProctype proc) {
        proctypes.put(name, proc);
        namedItems.put(name, proc);
    }

    public void putType(String name, SAPromelaType type) {
        types.put(name, type);
        namedItems.put(name, type);
    }
}
