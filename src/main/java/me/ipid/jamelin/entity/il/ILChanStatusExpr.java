package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.constant.PromelaLanguage.ChanStatusOp;
import me.ipid.jamelin.execute.Kernel;
import me.ipid.jamelin.execute.KnlChan;
import me.ipid.jamelin.execute.ProcessControlBlock;
import me.ipid.util.errors.Unreachable;

public class ILChanStatusExpr implements ILExpr {
    public final ILExpr chanId;
    public final ChanStatusOp op;

    public ILChanStatusExpr(ILExpr chanId, ChanStatusOp op) {
        this.chanId = chanId;
        this.op = op;
    }

    @Override
    public int execute(Kernel kernel, ProcessControlBlock procInfo, boolean noSideEffect) {
        KnlChan chan = kernel.getChannel(chanId.execute(kernel, procInfo, noSideEffect));

        switch (op) {
            case FULL:
                return chan.isFull() ? 1 : 0;
            case EMPTY:
                return chan.isEmpty() ? 1 : 0;
            case NFULL:
                return chan.isFull() ? 0 : 1;
            case NEMPTY:
                return chan.isEmpty() ? 0 : 1;
            default:
                throw new Unreachable();
        }
    }
}
