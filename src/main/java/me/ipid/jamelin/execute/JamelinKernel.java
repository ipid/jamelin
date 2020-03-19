package me.ipid.jamelin.execute;

import me.ipid.jamelin.entity.*;
import me.ipid.jamelin.entity.state.*;
import me.ipid.jamelin.entity.statement.*;
import me.ipid.jamelin.util.*;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class JamelinKernel {
    private RuntimeInfo info;
    private List<MemorySlot> globalMemory;

    private List<ProcessControlBlock> pcbList;

    public JamelinKernel(RuntimeInfo info) {
        this.info = info;

        this.globalMemory = new ArrayList<>();
        MemoryUtil.copyMemorySlots(info.getGlobalMemoryLayout(), this.globalMemory);

        this.pcbList = new ArrayList<>();
        ProcessControlBlock initPcb = new ProcessControlBlock(
                0, info.getInitProc().getMemoryLayout(), info.getInitProc().getStateMachine().getStart());
        this.pcbList.add(initPcb);
    }

    public MemorySlot getGlobalSlot(int i) {
        return globalMemory.get(i);
    }

    public ProcessControlBlock getPcb(int i) {
        return pcbList.get(i);
    }

    public void run() {
        while (true) {
            boolean res = runOnce();
            if (!res) {
                break;
            }
        }
    }

    /**
     * 执行一条语句
     *
     * @return 布尔值，True 表示本次执行了一次转移，False 表示本次找不到可执行的转移边
     */
    public boolean runOnce() {
        Optional<Pair<ProcessControlBlock, TransitionEdge>> maybeNext = findExecutable();
        if (!maybeNext.isPresent()) {
            return false;
        }
        Pair<ProcessControlBlock, TransitionEdge> next = maybeNext.get();

        for (PromelaStatement statement : next.getRight().getAction()) {
            statement.execute(this, next.getLeft());
        }
        next.getLeft().setCurrState(next.getRight().getTo());

        return true;
    }

    private Optional<Pair<ProcessControlBlock, TransitionEdge>> findExecutable() {

        int count = 0;
        Optional<Pair<ProcessControlBlock, TransitionEdge>> result = Optional.empty();

        for (ProcessControlBlock pcb : pcbList) {
            for (TransitionEdge edge : pcb.getCurrState().getOutEdge()) {
                if (edge.getCondition().execute(this, pcb) == 0) {
                    // 如果该 edge 不可执行
                    continue;
                }

                count++;

                // 使用 Reservoir sampling 算法节省内存
                int randNum = RandomUtils.nextInt(0, count);
                if (randNum == 0) {
                    result = Optional.of(Pair.of(pcb, edge));
                }
            }
        }

        return result;
    }
}
