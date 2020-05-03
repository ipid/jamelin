package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.execute.JamelinKernel;
import me.ipid.jamelin.execute.ProcessControlBlock;

import java.util.List;

public class ILRangeSendArg implements ILSendArgItem {
    public final ILRange ilRange;

    public ILRangeSendArg(ILRange ilRange) {
        this.ilRange = ilRange;
    }

    @Override
    public void fillValue(JamelinKernel kernel, ProcessControlBlock pcb, List<? super Integer> target) {
        var range = ilRange.execute(kernel, pcb);

        if (ilRange.global) {
            for (int i = range.a; i < range.b; i++) {
                target.add(kernel.getGlobalMemory(i));
            }
        } else {
            for (int i = range.a; i < range.b; i++) {
                target.add(pcb.getProcessMemory(i));
            }
        }
    }
}
