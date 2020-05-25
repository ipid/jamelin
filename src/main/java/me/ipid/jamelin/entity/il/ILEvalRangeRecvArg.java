package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.exception.RuntimeExceptions.JamelinRuntimeException;
import me.ipid.jamelin.execute.Kernel;
import me.ipid.jamelin.execute.ProcessControlBlock;

public class ILEvalRangeRecvArg implements ILRecvArgItem {
    public final int size;
    public final ILRange ilRange;

    public ILEvalRangeRecvArg(int size, ILRange ilRange) {
        this.size = size;
        this.ilRange = ilRange;
    }

    @Override
    public boolean receivable(Kernel kernel, ProcessControlBlock pcb,
                              int[] msg, int startIn, int endEx) {
        var range = ilRange.execute(kernel, pcb);
        if (endEx - startIn != range.b - range.a) {
            throw new JamelinRuntimeException("接收消息时发生错误：消息类型与 ILEvalRangeRecvArg 不符");
        }

        if (ilRange.global) {
            for (int i = 0; i < range.b - range.a; i++) {
                if (kernel.getGlobalMemory(range.a + i) != msg[startIn + i]) {
                    return false;
                }
            }
        } else {
            for (int i = 0; i < range.b - range.a; i++) {
                if (pcb.getProcessMemory(range.a + i) != msg[startIn + i]) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public void receiveValue(Kernel kernel, ProcessControlBlock pcb,
                             int[] msg, int startIn, int endEx) {
        // 不需要做任何事情
        var range = ilRange.execute(kernel, pcb);
        if (endEx - startIn != range.b - range.a) {
            throw new JamelinRuntimeException("接收消息时发生错误：消息类型与 ILEvalRangeRecvArg 不符");
        }
    }
}
