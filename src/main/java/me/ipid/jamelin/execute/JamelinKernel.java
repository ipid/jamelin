package me.ipid.jamelin.execute;

import me.ipid.jamelin.entity.RuntimeInfo;
import me.ipid.jamelin.entity.il.ILProctype;
import me.ipid.jamelin.entity.il.ILStatement;
import me.ipid.jamelin.entity.state.StateNode;
import me.ipid.jamelin.entity.state.TransitionEdge;
import me.ipid.jamelin.exception.CompileExceptions.OutOfLimitException;
import me.ipid.util.tupling.Tuple2;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class JamelinKernel {

    private static final Logger log = LogManager.getLogger(JamelinKernel.class);
    private static final int MAX_PID_INCLUSIVE = 255;

    // 保留 RuntimeInfo，用于支持 run 等表达式
    private final RuntimeInfo info;
    private final ArrayList<Integer> globalMemory;
    private final List<ProcessControlBlock> pcbList, nextTickNewPcbs;
    private final Deque<Integer> unusedPid;

    public JamelinKernel(RuntimeInfo info) {
        this.info = info;
        this.globalMemory = new ArrayList<>(info.globalMemory);

        this.pcbList = new ArrayList<>();
        this.nextTickNewPcbs = new ArrayList<>();
        int lastUnusedPid = fillPcbList(info.activeProcs, this.pcbList);

        this.unusedPid = new LinkedList<>();
        for (int i = lastUnusedPid; i <= MAX_PID_INCLUSIVE; i++) {
            unusedPid.addLast(i);
        }
    }

    /**
     * 启动一个新进程。
     */
    public int createProcess(int procSerialNum) {
        if (unusedPid.isEmpty()) {
            throw new OutOfLimitException("pid 已经用尽，无法创建新进程");
        }

        int pid = unusedPid.removeFirst();
        ILProctype proc = info.procs.get(procSerialNum);
        nextTickNewPcbs.add(new ProcessControlBlock(
                proc.name, pid, proc.memory, proc.getStart(), proc.getEnd()));

        return pid;
    }

    public int getGlobalMemory(int offset) {
        return globalMemory.get(offset);
    }

    public ProcessControlBlock getPcb(int i) {
        return pcbList.get(i);
    }

    public void run() {
        // 调用初始化语句
        runInitStatements();

        // 输出当前 PCB 中的进程
        printProcessList();

        while (true) {
            boolean res = runOnce();
            if (!res) {
                break;
            }
        }

        log.debug("所有语句执行完毕");
    }

    private void printProcessList() {
        log.debug("当前正在运行的进程：" + pcbList.stream()
                .map(x -> "<" + x.name + ">")
                .collect(Collectors.joining(", ")));
    }

    /**
     * 执行一条语句
     *
     * @return 布尔值，True 表示本次执行了一次转移，False 表示本次找不到可执行的转移边
     */
    public boolean runOnce() {
        var maybeNext = findExecutable();
        if (maybeNext.isEmpty()) {
            log.debug("找不到可执行的语句");
            return false;
        }

        Tuple2<ProcessControlBlock, TransitionEdge> next = maybeNext.get();

        // 执行转移边上的动作
        for (ILStatement statement : next.b.getAction()) {
            statement.execute(this, next.a);
        }
        // 转移边上的条件表达式可能有副作用，也需要执行
        next.b.getCondition().execute(this, next.a);

        // 改变当前进程的状态
        next.a.setCurrState(next.b.getTo());

        if (next.a.getCurrState() == next.a.end) {
            // 进程运行结束，清理 PCB
            log.debug("<" + next.a.name + "> 进程运行结束");
            pcbList.remove(next.a);
        }

        // 如果刚刚调用了 run 语句，则将 nextTickNewPcbs 中的内容加入 pcbList
        if (!nextTickNewPcbs.isEmpty()) {
            for (ProcessControlBlock pcb: nextTickNewPcbs) {
                pcbList.add(pcb);
                log.debug("启动 <" + pcb.name + "> 进程");
            }
            nextTickNewPcbs.clear();

            printProcessList();
        }

        return true;
    }

    public void setGlobalMemory(int offset, int newValue) {
        globalMemory.set(offset, newValue);
    }

    /**
     * 通过 ILProctype 对象建立 PCB 块。
     *
     * @param procs   ILProctype 列表
     * @param pcbList PCB 块列表
     * @return 最后一个未被使用的 pid
     */
    private static int fillPcbList(
            List<ILProctype> procs, List<ProcessControlBlock> pcbList) {
        int pid = 0;

        for (ILProctype proc : procs) {
            if (pid > MAX_PID_INCLUSIVE) {
                throw new OutOfLimitException("进程数量超过个数限制（" + MAX_PID_INCLUSIVE + " 个）");
            }

            pcbList.add(new ProcessControlBlock(
                    proc.name, pid, proc.memory, proc.getStart(), proc.getEnd()));
            pid++;
        }

        return pid;
    }

    private Optional<Tuple2<ProcessControlBlock, TransitionEdge>> findExecutable() {

        int count = 0;
        Optional<Tuple2<ProcessControlBlock, TransitionEdge>> result = Optional.empty();

        for (ProcessControlBlock pcb : pcbList) {
            for (TransitionEdge edge : pcb.getCurrState().outEdge) {
                // 转移边上的条件可能有副作用，因此只调用 checkCond 方法
                if (!edge.getCondition().checkCond(this, pcb)) {
                    // 如果该 edge 的条件是 false，即不可执行
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

    private void runInitStatements() {
        ProcessControlBlock pcb = new MockPCB();

        for (ILStatement statement : info.initStatements) {
            statement.execute(this, pcb);
        }
    }
}
