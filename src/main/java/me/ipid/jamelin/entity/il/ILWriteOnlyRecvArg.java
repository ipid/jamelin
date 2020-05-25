package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.execute.Kernel;
import me.ipid.jamelin.execute.ProcessControlBlock;

public class ILWriteOnlyRecvArg implements ILRecvArgItem {
    public final static ILWriteOnlyRecvArg INSTANCE = new ILWriteOnlyRecvArg();

    @Override
    public boolean receivable(
            Kernel kernel, ProcessControlBlock pcb,
            int[] msg, int startIn, int endEx
    ) {
        // 由于是只写参数，所以永远可写
        return true;
    }

    @Override
    public void receiveValue(Kernel kernel, ProcessControlBlock pcb, int[] msg, int startIn, int endEx) {
        // 由于是只写参数，所以永远不需要做任何工作
    }
}
