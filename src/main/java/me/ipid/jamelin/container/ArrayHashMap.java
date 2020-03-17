package me.ipid.jamelin.container;

import java.util.*;

public class ArrayHashMap<K, V> {

    private final ArrayList<V> arr = new ArrayList<>();
    private final HashMap<K, Integer> indexMap = new HashMap<>();

    public int size() {
        return indexMap.size();
    }

    public boolean isEmpty() {
        return indexMap.isEmpty();
    }

    public boolean containsKey(K key) {
        return indexMap.containsKey(key);
    }

    public boolean containsValue(V value) {
        return arr.contains(value);
    }

    public Optional<V> get(K key) {
        Integer targetIndex = indexMap.get(key);
        if (targetIndex == null) {
            return Optional.empty();
        }

        return Optional.of(arr.get(targetIndex));
    }

    public Optional<Integer> getIndex(K key) {
        return Optional.ofNullable(indexMap.get(key));
    }

    public Optional<V> put(K key, V value) {
        if (key == null || value == null) {
            throw new RuntimeException("key 与 value 不能为 null");
        }

        Integer oldTargetIndex = indexMap.get(key);
        Optional<V> result = Optional.empty();

        if (oldTargetIndex == null) {
            arr.add(value);
            indexMap.put(key, arr.size() - 1);
        } else {
            result = Optional.of(arr.set(oldTargetIndex, value));
        }

        return result;
    }

    /**
     * 删除指定 key 对应的元素。
     * 警告：本操作具有 O(n) 时间复杂度。
     *
     * @param key: 想要删掉的元素的 key
     * @return 旧元素；当没有旧元素时返回 null
     */
    public V remove(K key) {
        Integer oldIndexBox = indexMap.get(key);
        if (oldIndexBox == null) {
            return null;
        }

        // 删除旧元素
        int oldIndex = oldIndexBox;
        V result = arr.remove(oldIndex);
        indexMap.remove(key);

        // 将 oldIndex 之后的索引向前移
        indexMap.replaceAll((k, v) -> v > oldIndex ? v - 1 : v);

        return result;
    }

    public void putAll(Map<? extends K, ? extends V> m) {
        m.forEach(this::put);
    }

    public void clear() {
        arr.clear();
        indexMap.clear();
    }
}
