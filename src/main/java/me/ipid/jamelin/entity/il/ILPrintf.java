package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.execute.Kernel;
import me.ipid.jamelin.execute.ProcessControlBlock;

import java.util.List;

public class ILPrintf implements ILStatement {

    private String template;
    private List<ILExpr> paramExprList;

    public ILPrintf(String template, List<ILExpr> paramExprList) {
        this.template = template;
        this.paramExprList = paramExprList;
    }

    @Override
    public void execute(Kernel kernel, ProcessControlBlock procInfo) {
        System.out.printf(template, paramExprList
                .stream()
                .map(x -> x.execute(kernel, procInfo, false))
                .toArray());
    }
}
