package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.execute.Kernel;
import me.ipid.jamelin.execute.KnlChan;
import me.ipid.jamelin.execute.ProcessControlBlock;

import java.util.List;

public class ILChanPollExpr implements ILExpr {

    public final ILExpr ilChanId;
    public final List<Integer> typeIds;
    public final List<ILRecvArgItem> args;

    public ILChanPollExpr(List<ILRecvArgItem> args, ILExpr ilChanId, List<Integer> typeIds) {
        this.ilChanId = ilChanId;
        this.typeIds = typeIds;
        this.args = args;
    }

    @Override
    public int execute(Kernel kernel, ProcessControlBlock procInfo, boolean noSideEffect) {
        int chanId = ilChanId.execute(kernel, procInfo, noSideEffect);
        KnlChan chan = kernel.getChannel(chanId);

        // 如果 type id 不符，抛出异常
        chan.checkTypeIdConsistency(typeIds);

        // 如果没有消息，或者消息不可接收，返回 0
        if (!chan.receivable(kernel, procInfo, args)) {
            return 0;
        }

        return 1;
    }
}
