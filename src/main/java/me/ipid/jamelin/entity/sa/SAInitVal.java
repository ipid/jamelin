package me.ipid.jamelin.entity.sa;

import java.util.function.Consumer;
import java.util.function.Function;

public interface SAInitVal {
    void visit(
            Consumer<SASingleInitVal> singleInitVal,
            Consumer<SAInitList> initList,
            Consumer<SANoInit> noInit
    );
}
