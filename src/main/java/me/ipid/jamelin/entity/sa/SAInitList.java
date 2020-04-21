package me.ipid.jamelin.entity.sa;

import java.util.function.Consumer;
import java.util.function.Function;

public final class SAInitList implements SAInitVal {

    public final int[] nums;

    public SAInitList(int[] nums) {
        this.nums = nums;
    }

    @Override
    public void visit(Consumer<SASingleInitVal> singleInitVal, Consumer<SAInitList> initList, Consumer<SANoInit> noInit) {
        initList.accept(this);
    }
}
