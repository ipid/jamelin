package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.constant.PromelaLanguage.PredefVar;
import me.ipid.jamelin.exception.CompileExceptions.NotSupportedException;
import me.ipid.jamelin.execute.JamelinKernel;
import me.ipid.jamelin.execute.ProcessControlBlock;

public class ILPredefExpr implements ILExpr {

    public final PredefVar pre;

    public ILPredefExpr(PredefVar pre) {
        this.pre = pre;
    }

    @Override
    public int execute(JamelinKernel kernel, ProcessControlBlock procInfo, boolean noSideEffect) {
        // TODO: 支持 else、timeout 等预定义变量

        switch (pre) {
            case PID:
                return procInfo.getPid();
            case ELSE:
                return procInfo.isElse() ? 1 : 0;
            case TIMEOUT:
                return kernel.isTimeout() ? 1 : 0;
            case NR_PR:
                return kernel.getProcessNum();
            default:
                throw new NotSupportedException("暂不支持其它预定义变量");
        }
    }
}
