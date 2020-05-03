package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.execute.JamelinKernel;
import me.ipid.jamelin.execute.ProcessControlBlock;

import java.util.List;

public interface ILSendArgItem {
    /**
     * 向 target 中填入消息的真实值。
     */
    void fillValue(JamelinKernel kernel, ProcessControlBlock pcb, List<? super Integer> target);
}
