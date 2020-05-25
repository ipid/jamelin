package me.ipid.jamelin.execute;

import me.ipid.jamelin.entity.RuntimeInfo;
import me.ipid.jamelin.entity.il.ILProctype;
import me.ipid.jamelin.entity.il.ILStatement;
import me.ipid.jamelin.entity.state.TransitionEdge;
import me.ipid.jamelin.exception.CompileExceptions.OutOfLimitException;
import me.ipid.jamelin.exception.RuntimeExceptions.JamelinRuntimeException;
import me.ipid.jamelin.util.Slot;
import me.ipid.util.random.Reservoir;
import me.ipid.util.tupling.Tuple2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class Kernel {

    private static final Logger log = LogManager.getLogger(Kernel.class);
    private static final int
            MAX_PID_INCLUSIVE = 254,
            MAX_CHAN_INCLUSIVE = 254;

    // 保留 RuntimeInfo，用于支持 run 等表达式
    private final RuntimeInfo info;
    private final ArrayList<Integer> globalMemory;
    private final List<ProcessControlBlock> pcbList, nextTickNewPcbs;
    private final Deque<Integer> unusedPid;
    private final List<KnlChan> chans;
    private boolean timeout;

    public Kernel(RuntimeInfo info) {
        this.info = info;
        this.globalMemory = new ArrayList<>(info.globalMemory);

        this.pcbList = new ArrayList<>();
        this.nextTickNewPcbs = new ArrayList<>();
        int lastUnusedPid = fillPcbList(info.activeProcs, this.pcbList);

        this.unusedPid = new LinkedList<>();
        for (int i = lastUnusedPid; i <= MAX_PID_INCLUSIVE; i++) {
            unusedPid.addLast(i);
        }

        this.chans = new ArrayList<>();
        chans.add(null);

        this.timeout = false;
    }

    public int getProcessNum() {
        return pcbList.size();
    }

    public boolean isTimeout() {
        return timeout;
    }

    public int createChan(
            int bufLen, List<Slot> slots, List<Integer> typeIds, List<Integer> msgUnitLen, int msgSizeCache
    ) {
        if (chans.size() > MAX_CHAN_INCLUSIVE) {
            throw new JamelinRuntimeException("信道数超过 " + MAX_CHAN_INCLUSIVE + " 个，无法创建新信道");
        }

        chans.add(new KnlChan(bufLen, slots, typeIds, msgUnitLen, msgSizeCache));
        return chans.size() - 1;
    }

    /**
     * 启动一个新进程。
     */
    public int createProcess(int procSerialNum) {
        if (unusedPid.isEmpty()) {
            throw new JamelinRuntimeException("pid 已经用尽，无法创建新进程");
        }

        int pid = unusedPid.removeFirst();
        ILProctype proc = info.procs.get(procSerialNum);
        nextTickNewPcbs.add(new ProcessControlBlock(
                proc.name, pid, proc.memory, proc.getStart(), proc.getEnd()));

        return pid;
    }

    public KnlChan getChannel(int chanId) {
        if (chanId <= 0) {
            throw new JamelinRuntimeException("试图使用未初始化的信道");
        }
        if (chanId > MAX_CHAN_INCLUSIVE) {
            throw new JamelinRuntimeException("试图获取超过信道数量限制的信道");
        }
        if (chanId >= chans.size()) {
            throw new JamelinRuntimeException("试图获取尚未存在的信道");
        }

        return Objects.requireNonNull(chans.get(chanId));
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

        if (pcbList.size() > 0) {
            log.error("系统发生死锁");
        } else {
            log.debug("所有语句执行完毕");
        }
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
            if (pid >= MAX_PID_INCLUSIVE) {
                throw new OutOfLimitException("进程数量超过个数限制（" + MAX_PID_INCLUSIVE + " 个）");
            }

            pcbList.add(new ProcessControlBlock(
                    proc.name, pid, proc.memory, proc.getStart(), proc.getEnd()));
            pid++;
        }

        return pid;
    }

    private Optional<Tuple2<ProcessControlBlock, TransitionEdge>> findExec() {
        // 在此函数中处理 timeout 的逻辑
        timeout = false;
        Optional<Tuple2<ProcessControlBlock, TransitionEdge>> result = findExecOfAllProcess();

        if (result.isEmpty()) {
            // 如果没有，那就设成 true 再搞一遍
            timeout = true;

            // 模拟原版 SPIN 的功能
            if (pcbList.size() > 0) {
                log.info("发生 timeout");
            }

            result = findExecOfAllProcess();
        }

        // 如果在此处还是 empty，那就没办法了
        return result;
    }

    private Optional<Tuple2<ProcessControlBlock, TransitionEdge>> findExecOfAllProcess() {
        // 在此函数中处理 else 的逻辑
        Reservoir<Tuple2<ProcessControlBlock, TransitionEdge>> reservoir
                = new Reservoir<>(1);

        for (ProcessControlBlock pcb : pcbList) {
            pcb.setElse(false);
            var curr = findExecOfPcb(pcb);

            if (curr.isEmpty()) {
                // 如果没有，那就设成 true 再搞一遍
                pcb.setElse(true);
                curr = findExecOfPcb(pcb);
            }

            // 如果找到了（可能是第一遍或第二遍找到的），那就放进水库里
            curr.ifPresent(reservoir::feed);
        }

        return reservoir.get(0);
    }

    private Optional<Tuple2<ProcessControlBlock, TransitionEdge>> findExecOfPcb(
            ProcessControlBlock pcb
    ) {
        Reservoir<Tuple2<ProcessControlBlock, TransitionEdge>> reservoir
                = new Reservoir<>(1);

        for (TransitionEdge edge : pcb.getCurrState().outEdge) {
            // 转移边上的条件可能有副作用，因此只调用 checkCond 方法
            if (edge.condition.execute(this, pcb, true) == 0) {
                // 如果该 edge 的条件是 false，即不可执行
                continue;
            }

            // 若该转移边满足条件，将该转移边流过水库
            reservoir.feed(Tuple2.of(pcb, edge));
        }

        return reservoir.get(0);
    }

    private void printProcessList() {
        log.debug("当前正在运行的进程：" + pcbList.stream()
                .map(x -> "<" + x.name + ">")
                .collect(Collectors.joining(", ")));
    }

    private void runInitStatements() {
        ProcessControlBlock pcb = new MockPCB();

        for (ILStatement statement : info.initStatements) {
            statement.execute(this, pcb);
        }
    }

    /**
     * 执行一条语句
     *
     * @return 布尔值，True 表示本次执行了一次转移，False 表示本次找不到可执行的转移边
     */
    private boolean runOnce() {
        var maybeNext = findExec();
        if (maybeNext.isEmpty()) {
            // 注意「找不到可执行的语句」不一定是死锁，有可能是全部执行完了
            log.debug("找不到可执行的语句");
            return false;
        }

        Tuple2<ProcessControlBlock, TransitionEdge> next = maybeNext.get();

        // 执行转移边上的动作
        for (ILStatement statement : next.b.action) {
            statement.execute(this, next.a);
        }

        // 转移边上的条件表达式可能有副作用，也需要执行
        next.b.condition.execute(this, next.a, false);

        // 改变当前进程的状态
        next.a.setCurrState(next.b.to);

        if (next.a.getCurrState() == next.a.end) {
            // 进程运行结束，清理 PCB
            log.debug("<" + next.a.name + "> 进程运行结束");
            pcbList.remove(next.a);
            unusedPid.addLast(next.a.getPid());
        }

        // 如果刚刚调用了 run 语句，则将 nextTickNewPcbs 中的内容加入 pcbList
        if (!nextTickNewPcbs.isEmpty()) {
            for (ProcessControlBlock pcb : nextTickNewPcbs) {
                pcbList.add(pcb);
                log.debug("启动 <" + pcb.name + "> 进程");
            }
            nextTickNewPcbs.clear();

            printProcessList();
        }

        return true;
    }
}
