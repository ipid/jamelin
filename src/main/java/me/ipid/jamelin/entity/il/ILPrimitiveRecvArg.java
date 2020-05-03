package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.exception.RuntimeExceptions.JamelinRuntimeException;
import me.ipid.jamelin.execute.JamelinKernel;
import me.ipid.jamelin.execute.ProcessControlBlock;
import me.ipid.jamelin.util.Slot;

public class ILPrimitiveRecvArg implements ILRecvArgItem {

    public final Slot slot;
    public final ILExpr ilMemLoc;
    public final boolean global;

    public ILPrimitiveRecvArg(Slot slot, ILExpr ilMemLoc, boolean global) {
        this.slot = slot;
        this.ilMemLoc = ilMemLoc;
        this.global = global;
    }

    private void checkMsgLen(int startIn, int endEx) {
        if (endEx - startIn != 1) {
            throw new JamelinRuntimeException("接收消息时发生错误：消息类型与 ILPrimitiveRecvArg 不符");
        }
    }

    @Override
    public boolean receivable(
            JamelinKernel kernel, ProcessControlBlock pcb,
                              int[] msg, int startIn, int endEx
    ) {
        checkMsgLen(startIn, endEx);
        return true;
    }

    @Override
    public void receiveValue(
            JamelinKernel kernel, ProcessControlBlock pcb,
            int[] msg, int startIn, int endEx
    ) {
        checkMsgLen(startIn, endEx);

        int memLoc = ilMemLoc.execute(kernel, pcb, false);
        if (global) {
            kernel.setGlobalMemory(memLoc, msg[startIn]);
        } else {
            pcb.setProcessMemory(memLoc, msg[startIn]);
        }
    }
}
