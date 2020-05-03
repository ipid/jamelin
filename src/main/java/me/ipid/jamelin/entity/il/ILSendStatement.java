package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.execute.JamelinKernel;
import me.ipid.jamelin.execute.KnlChan;
import me.ipid.jamelin.execute.ProcessControlBlock;

import java.util.List;

public class ILSendStatement implements ILStatement {

    public final ILExpr chanId;
    public final List<ILSendArgItem> sendArgs;
    public final List<Integer> typeIds;

    public ILSendStatement(ILExpr chanId, List<ILSendArgItem> sendArgs, List<Integer> typeIds) {
        this.chanId = chanId;
        this.sendArgs = sendArgs;
        this.typeIds = typeIds;
    }

    @Override
    public void execute(JamelinKernel kernel, ProcessControlBlock procInfo) {
        KnlChan chan = kernel.getChannel(chanId.execute(kernel, procInfo, false));

        chan.checkTypeIdConsistency(typeIds);
        chan.sendMessage(kernel, procInfo, sendArgs);
    }
}
