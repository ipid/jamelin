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
    public int execute(JamelinKernel kernel, ProcessControlBlock procInfo) {
        switch (pre) {
            case PID:
                return procInfo.getPid();
            default:
                throw new NotSupportedException("暂不支持其它预定义变量");
        }
    }
}
