package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.exception.RuntimeExceptions.JamelinRuntimeException;
import me.ipid.jamelin.execute.Kernel;
import me.ipid.jamelin.execute.ProcessControlBlock;

public class ILConstRecvArg implements ILRecvArgItem {
    public final int num;

    public ILConstRecvArg(int num) {
        this.num = num;
    }

    private void checkMsgLength(int startIn, int endEx) {
        if (endEx - startIn != 1) {
            throw new JamelinRuntimeException("试图接收消息时出错：消息类型不符");
        }
    }

    @Override
    public boolean receivable(Kernel kernel, ProcessControlBlock pcb, int[] msg, int startIn, int endEx) {
        checkMsgLength(startIn, endEx);
        return msg[startIn] == num;
    }

    @Override
    public void receiveValue(Kernel kernel, ProcessControlBlock pcb, int[] msg, int startIn, int endEx) {
        // 接收参数为常数时，无需进行任何操作
        checkMsgLength(startIn, endEx);
    }
}
