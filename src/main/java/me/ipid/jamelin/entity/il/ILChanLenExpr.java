package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.execute.Kernel;
import me.ipid.jamelin.execute.ProcessControlBlock;

public class ILChanLenExpr implements ILExpr {
    public final ILExpr chanId;

    public ILChanLenExpr(ILExpr chanId) {
        this.chanId = chanId;
    }

    @Override
    public int execute(Kernel kernel, ProcessControlBlock procInfo, boolean noSideEffect) {
        int id = chanId.execute(kernel, procInfo, noSideEffect);
        return kernel.getChannel(id).countMessage();
    }
}
