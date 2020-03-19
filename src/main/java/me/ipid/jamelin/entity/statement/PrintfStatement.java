package me.ipid.jamelin.entity.statement;

import me.ipid.jamelin.entity.*;
import me.ipid.jamelin.entity.expr.*;
import me.ipid.jamelin.execute.*;

import java.util.List;

public class PrintfStatement implements PromelaStatement {

    private String template;
    private List<PromelaExpr> paramExprList;

    public PrintfStatement(String template, List<PromelaExpr> paramExprList) {
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
