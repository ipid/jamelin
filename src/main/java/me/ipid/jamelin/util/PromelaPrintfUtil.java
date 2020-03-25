package me.ipid.jamelin.util;

import com.google.common.collect.Lists;
import me.ipid.jamelin.compiler.PrintVisitor.*;
import me.ipid.jamelin.exception.*;

public class PromelaPrintfUtil {

    /**
     * 解析 Promela 中 printf 的模板字符串。
     * @param printTemplate 模板字符串（已 unescape，不含两端的引号）
     * @return % 参数的个数
     */
    public static int parseTemplate(String printTemplate) {
        int index = 0;
        State state = State.INITIAL;

        for (char c : Lists.charactersOf(printTemplate)) {
            if (state == State.INITIAL) {
                if (c == '%') {
                    state = State.PERCENT;
                }
            } else {
                if (c == 'd') {
                    index++;
                    state = State.INITIAL;
                } else if (c == '%') {
                    state = State.INITIAL;
                } else {
                    throw new NotSupportedException("暂不支持 %d 以外的 printf 转义字符");
                }
            }
        }

        return index;
    }
}
