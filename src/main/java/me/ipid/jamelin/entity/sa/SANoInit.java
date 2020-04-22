package me.ipid.jamelin.entity.sa;

import java.util.function.Consumer;

public final class SANoInit implements SAInitVal {

    private static SANoInit singleton = null;

    public static SANoInit instance() {
        if (singleton == null) {
            singleton = new SANoInit();
        }

        return singleton;
    }

    @Override
    public void visit(Consumer<SASingleInitVal> singleInitVal,
                      Consumer<SAInitList> initList, Consumer<SANoInit> noInit) {
        noInit.accept(this);
    }
}