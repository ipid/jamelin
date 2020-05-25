package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.exception.RuntimeExceptions.JamelinRuntimeException;
import me.ipid.jamelin.execute.Kernel;
import me.ipid.jamelin.execute.ProcessControlBlock;

public class ILEvalExprRecvArg implements ILRecvArgItem {

    public final ILExpr matchExpr;

    public ILEvalExprRecvArg(ILExpr matchExpr) {
        this.matchExpr = matchExpr;
    }

    private void checkMsgLength(int startIn, int endEx) {
        if (endEx - startIn != 1) {
            throw new JamelinRuntimeException("接收消息时发生错误：消息类型与 ILEvalExprRecvArg 不符");
        }
    }

    @Override
    public boolean receivable(Kernel kernel, ProcessControlBlock pcb,
                              int[] msg, int startIn, int endEx) {
        checkMsgLength(startIn, endEx);

        int exprValue = matchExpr.execute(kernel, pcb, true);
        return msg[startIn] == exprValue;
    }

    @Override
    public void receiveValue(Kernel kernel, ProcessControlBlock pcb,
                             int[] msg, int startIn, int endEx) {
        // eval(...) 的接收参数，接收消息时无需任何操作
        checkMsgLength(startIn, endEx);
    }
}
