package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.exception.RuntimeExceptions.JamelinRuntimeException;
import me.ipid.jamelin.execute.JamelinKernel;
import me.ipid.jamelin.execute.ProcessControlBlock;

public class ILRangeRecvArg implements ILRecvArgItem {
    public final int size;
    public final ILRange ilRange;

    public ILRangeRecvArg(int size, ILRange ilRange) {
        this.size = size;
        this.ilRange = ilRange;
    }

    @Override
    public boolean receivable(
            JamelinKernel kernel, ProcessControlBlock pcb,
            int[] msg, int startIn, int endEx
    ) {
        var range = ilRange.execute(kernel, pcb);
        if (endEx - startIn != range.b - range.a) {
            throw new JamelinRuntimeException("接收消息时发生错误：消息类型与 ILRangeRecvArg 不符");
        }

        // 能接收任意消息
        return true;
    }

    @Override
    public void receiveValue(
            JamelinKernel kernel, ProcessControlBlock pcb,
            int[] msg, int startIn, int endEx
    ) {
        var range = ilRange.execute(kernel, pcb);
        if (endEx - startIn != range.b - range.a) {
            throw new JamelinRuntimeException("接收消息时发生错误：消息类型与 ILRangeRecvArg 不符");
        }

        if (ilRange.global) {
            for (int i = 0; i < range.b - range.a; i++) {
                kernel.setGlobalMemory(range.a + i, msg[startIn + i]);
            }
        } else {
            for (int i = 0; i < range.b - range.a; i++) {
                pcb.setProcessMemory(range.a + i, msg[startIn + i]);
            }
        }
    }
}
