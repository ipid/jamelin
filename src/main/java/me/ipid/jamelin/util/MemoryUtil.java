package me.ipid.jamelin.util;

import me.ipid.jamelin.entity.*;

import java.util.List;

public class MemoryUtil {
    public static void copyMemorySlots(List<MemorySlot> source, List<MemorySlot> dest) {
        for (MemorySlot slot : source) {
            try {
                dest.add((MemorySlot) slot.clone());
            } catch (CloneNotSupportedException e) {
                throw new Error("MemorySlot 无法克隆");
            }
        }
    }
}
