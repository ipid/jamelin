package me.ipid.jamelin.execute;

import me.ipid.jamelin.entity.il.ILRecvArgItem;
import me.ipid.jamelin.entity.il.ILSendArgItem;
import me.ipid.jamelin.entity.sa.SATypeFactory;
import me.ipid.jamelin.exception.RuntimeExceptions.JamelinRuntimeException;
import me.ipid.jamelin.util.Slot;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class KnlChan {
    private final int bufLen;
    private final List<Slot> slots;
    private final List<Integer> typeIds, msgUnitsLen;
    private final int msgSizeCache;

    // 不限容量的消息缓冲区，请自行限定容量
    private final Deque<int[]> msgs;

    public KnlChan(int bufLen, List<Slot> slots, List<Integer> typeIds,
                   List<Integer> msgUnitsLen, int msgSizeCache) {
        assert bufLen >= 1;

        this.bufLen = bufLen;
        this.typeIds = new ArrayList<>(typeIds);
        this.msgUnitsLen = new ArrayList<>(msgUnitsLen);
        this.slots = new ArrayList<>(slots);
        this.msgSizeCache = msgSizeCache;

        this.msgs = new LinkedList<>();
    }

    public boolean isEmpty() {
        return msgs.size() <= 0;
    }

    public boolean isFull() {
        return msgs.size() >= bufLen;
    }

    /**
     * 检查传入的 type id 与自己的 type id 是否一致。
     * 规则：
     * - 如果列表长度不一致，则不一致
     * - 如果传入值大于 type id 的最大值，则认为可接受“任意 type id”，视作一致
     *
     * @param others type id 的列表
     */
    public void checkTypeIdConsistency(List<Integer> others) {
        if (!isTypeIdConsistent(others)) {
            throw new JamelinRuntimeException("信道消息类型与传入的类型不一致");
        }
    }

    public int countMessage() {
        return msgs.size();
    }

    public boolean receivable(
            JamelinKernel kernel, ProcessControlBlock pcb,
            List<ILRecvArgItem> args
    ) {
        if (msgs.isEmpty()) {
            return false;
        }

        assert msgUnitsLen.size() == args.size();
        int[] msg = firstMsg();

        int start = 0;
        for (int i = 0; i < args.size(); i++) {
            ILRecvArgItem arg = args.get(i);
            int argLen = msgUnitsLen.get(i);

            if (!arg.receivable(kernel, pcb, msg, start, start + argLen)) {
                return false;
            }
            start += argLen;
        }

        return true;
    }

    public void receiveMessage(
            JamelinKernel kernel, ProcessControlBlock pcb,
            List<ILRecvArgItem> args, boolean peek
    ) {
        assert msgUnitsLen.size() == args.size();
        int[] msg = firstMsg();

        int start = 0;
        for (int i = 0; i < args.size(); i++) {
            ILRecvArgItem arg = args.get(i);
            int argLen = msgUnitsLen.get(i);

            arg.receiveValue(kernel, pcb, msg, start, start + argLen);
            start += argLen;
        }

        if (!peek) {
            msgs.removeFirst();
        }
    }

    public void sendMessage(
            JamelinKernel kernel, ProcessControlBlock pcb,
            List<ILSendArgItem> sendArgs
    ) {
        assert msgUnitsLen.size() == sendArgs.size();
        assert !isFull();

        // 将发送参数所表示的数据逐个收集
        List<Integer> messageBuf = new ArrayList<>();
        for (ILSendArgItem arg : sendArgs) {
            arg.fillValue(kernel, pcb, messageBuf);
        }

        // 将发送的消息构造成数组
        if (messageBuf.size() != msgSizeCache) {
            throw new JamelinRuntimeException("要发送的消息大小与信道消息大小不符合");
        }
        int[] msg = messageBuf.stream().mapToInt(x -> x).toArray();

        // 按照槽来舍入消息
        for (int i = 0; i < slots.size(); i++) {
            msg[i] = slots.get(i).cast(msg[i]);
        }

        // 将消息添加到缓冲区内
        msgs.addLast(msg);
    }

    private int[] firstMsg() {
        assert !msgs.isEmpty();
        return msgs.peekFirst();
    }

    private boolean isTypeIdConsistent(List<Integer> others) {
        if (others.size() != typeIds.size()) {
            return false;
        }

        for (int i = 0; i < typeIds.size(); i++) {
            int curr = typeIds.get(i), other = others.get(i);

            if (other > SATypeFactory.MAX_TYPE_ID) {
                continue;
            }
            if (curr != other) {
                return false;
            }
        }

        return true;
    }
}
