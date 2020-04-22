package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.execute.JamelinKernel;
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
    public void execute(JamelinKernel kernel, ProcessControlBlock procInfo) {
        System.out.printf(template, paramExprList
                .stream()
                .map(x -> x.execute(kernel, procInfo))
                .toArray());
    }
}
