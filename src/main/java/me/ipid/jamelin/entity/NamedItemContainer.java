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
        namedItems.putAll(initialTypes);
    }

    public Optional<ILNamedItem> getItem(String name) {
        return Optional.ofNullable(namedItems.get(name));
    }

    /**
     * 获取指定名字的 ILProctype 对象在 proctypes 数组中的顺序。
     * 此顺序按理来说，和 RuntimeInfo 中的顺序是一致的。
     *
     * @param name 进程名
     * @return 序列号
     */
    public int getSerialNumOfProc(String name) {
        return proctypes.indexOf(name);
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
