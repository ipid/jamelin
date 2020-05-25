package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.execute.Kernel;
import me.ipid.jamelin.execute.ProcessControlBlock;
import me.ipid.jamelin.util.Slot;

import java.util.ArrayList;
import java.util.List;

public class ILCrateChanExpr implements ILExpr {

    public final int bufLen;
    public final List<Slot> slots;
    public final List<Integer> typeIds, msgUnitLen;
    private final int msgSizeCache;

    public ILCrateChanExpr(int bufLen, List<Slot> slots, List<Integer> typeIds, List<Integer> msgUnitLen) {
        this.bufLen = bufLen;
        this.typeIds = new ArrayList<>(typeIds);
        this.slots = new ArrayList<>(slots);
        this.msgUnitLen = new ArrayList<>(msgUnitLen);

        this.msgSizeCache = msgUnitLen.stream().mapToInt(x -> x).sum();
    }

    @Override
    public int execute(Kernel kernel, ProcessControlBlock procInfo, boolean noSideEffect) {
        if (noSideEffect) {
            return 1;
        }

        return kernel.createChan(bufLen, slots, typeIds, msgUnitLen, msgSizeCache);
    }
}
