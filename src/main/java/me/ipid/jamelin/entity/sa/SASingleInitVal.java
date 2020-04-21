package me.ipid.jamelin.entity.sa;

import java.util.function.Consumer;
import java.util.function.Function;

public final class SASingleInitVal implements SAInitVal {

    public final int num;

    public SASingleInitVal(int num) {
        this.num = num;
    }

    @Override
    public void visit(Consumer<SASingleInitVal> singleInitVal,
                      Consumer<SAInitList> initList, Consumer<SANoInit> noInit) {
        singleInitVal.accept(this);
    }
}
