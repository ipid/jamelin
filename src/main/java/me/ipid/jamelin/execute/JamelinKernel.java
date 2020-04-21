package me.ipid.jamelin.execute;

import me.ipid.jamelin.ast.Ast.AstProctype;
import me.ipid.jamelin.entity.*;
import me.ipid.jamelin.entity.il.ILProctype;
import me.ipid.jamelin.entity.il.ILStatement;
import me.ipid.jamelin.entity.state.*;
import me.ipid.jamelin.util.*;
import me.ipid.util.tupling.Tuple2;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.tuple.Tuple2;

import java.util.*;

public class JamelinKernel {

    // 保留 RuntimeInfo，用于支持 run 等表达式
    private final RuntimeInfo info;
    private final ArrayList<Integer> globalMemory;
    private final List<ProcessControlBlock> pcbList;

    public JamelinKernel(RuntimeInfo info) {
        this.info = info;
        this.globalMemory = new ArrayList<Integer>(info.globalMemory);

        this.pcbList = new ArrayList<>();
        fillPcbList(info.activeProcs, this.pcbList);
    }

    public int getGlobalMemory(int offset) {
        return globalMemory.get(offset);
    }

    public void setGlobalMemory(int offset, int newValue) {
        globalMemory.set(offset, newValue);
    }

    private static void fillPcbList(
            List<ILProctype> procs, List<ProcessControlBlock> pcbList) {
        int pid = 0;

        for (var proc : procs) {
            pcbList.add(new ProcessControlBlock(pid, new ArrayList<>(), proc.stateMachine.getStart()));
            pid++;
        }
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
        Optional<Tuple2<ProcessControlBlock, TransitionEdge>> maybeNext = findExecutable();
        if (maybeNext.isEmpty()) {
            return false;
        }
        Tuple2<ProcessControlBlock, TransitionEdge> next = maybeNext.get();

        for (ILStatement statement : next.b.getAction()) {
            statement.execute(this, next.a);
        }
        next.a.setCurrState(next.b.getTo());

        return true;
    }

    private Optional<Tuple2<ProcessControlBlock, TransitionEdge>> findExecutable() {

        int count = 0;
        Optional<Tuple2<ProcessControlBlock, TransitionEdge>> result = Optional.empty();

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
                    result = Optional.of(Tuple2.of(pcb, edge));
                }
            }
        }

        return result;
    }
}
