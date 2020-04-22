package me.ipid.jamelin.entity.sa;

import java.util.function.Consumer;

public interface SAInitVal {
    void visit(
            Consumer<SASingleInitVal> singleInitVal,
            Consumer<SAInitList> initList,
            Consumer<SANoInit> noInit
    );
}
