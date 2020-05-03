package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.execute.JamelinKernel;
import me.ipid.jamelin.execute.ProcessControlBlock;

public interface ILRecvArgItem {
    /**
     * 检查当前消息是否可被接收。
     * 如果该参数为只写、变量参数等，则永远返回 true；只有当参数为 const、eval 的时候才会做检查。
     * 注意区分返回 false 与消息类型不符合的情况：当消息类型不符合（如消息长度与参数长度不匹配）时，抛出运行时异常。
     */
    boolean receivable(JamelinKernel kernel, ProcessControlBlock pcb,
                       int[] msg, int startIn, int endEx);

    /**
     * 将消息内容拷贝到真正的进程、全局内存里。
     */
    void receiveValue(JamelinKernel kernel, ProcessControlBlock pcb,
                      int[] msg, int startIn, int endEx);
}
