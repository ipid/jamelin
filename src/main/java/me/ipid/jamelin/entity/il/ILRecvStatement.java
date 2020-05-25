package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.execute.Kernel;
import me.ipid.jamelin.execute.KnlChan;
import me.ipid.jamelin.execute.ProcessControlBlock;

import java.util.List;

public class ILRecvStatement implements ILStatement {

    public final ILExpr ilChanId;
    public final List<Integer> typeIds;
    public final List<ILRecvArgItem> args;
    public final boolean peek;

    public ILRecvStatement(
            List<ILRecvArgItem> args,
            ILExpr ilChanId, List<Integer> typeIds,
            boolean peek
    ) {
        this.ilChanId = ilChanId;
        this.typeIds = typeIds;
        this.args = args;
        this.peek = peek;
    }

    @Override
    public void execute(Kernel kernel, ProcessControlBlock procInfo) {
        KnlChan chan = kernel.getChannel(ilChanId.execute(kernel, procInfo, false));

        // 检查 type id 一致性
        chan.checkTypeIdConsistency(typeIds);

        // 开始接收消息
        chan.receiveMessage(kernel, procInfo, args, peek);
    }
}
