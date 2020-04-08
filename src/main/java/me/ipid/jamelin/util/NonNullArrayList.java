package me.ipid.jamelin.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class NonNullArrayList<T> extends ArrayList<T> {

    public NonNullArrayList(int initialCapacity) {
        super(initialCapacity);
    }

    public NonNullArrayList() {
    }

    public NonNullArrayList(Collection<? extends T> c) {
        super(c);
        checkContainer(c);
    }

    private static void checkContainer(Collection<?> c) {
        if (!c.stream().allMatch(Objects::nonNull)) {
            throw new NullPointerException();
        }
    }

    @Override
    public boolean add(T t) {
        return super.add(Objects.requireNonNull(t));
    }

    @Override
    public void add(int index, T element) {
        super.add(index, Objects.requireNonNull(element));
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        checkContainer(c);
        return super.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        checkContainer(c);
        return super.addAll(index, c);
    }
}
