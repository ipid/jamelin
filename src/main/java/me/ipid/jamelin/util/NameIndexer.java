package me.ipid.jamelin.util;


import java.util.*;

public class NameIndexer {

    private Map<String, Integer> indexMap;
    private int lastUnused;

    public NameIndexer() {
        indexMap = new HashMap<>();
        lastUnused = 0;
    }

    public NameIndexer(int begin) {
        indexMap = new HashMap<>();
        lastUnused = begin;
    }

    public int put(String name) {
        int index = lastUnused;
        lastUnused++;

        Integer old = indexMap.put(name, index);
        if (old != null) {
            throw new Error("插入了已存在的名字");
        }

        return index;
    }

    public Optional<Integer> get(String name) {
        return Optional.ofNullable(indexMap.get(name));
    }
}
