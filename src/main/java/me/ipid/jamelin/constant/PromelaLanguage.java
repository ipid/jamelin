package me.ipid.jamelin.constant;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.Map;
import java.util.Set;

public class PromelaLanguage {

    public static final int ARR_INIT_VAL = 0;

    public static final ImmutableSet<String> primitiveTypes = ImmutableSet.<String>builder()
            .add("bit", "bool", "byte", "chan", "short", "int", "mtype", "pid")
            .build();

    public enum BinaryOp {
        ADD, SUB, MUL, DIV, MOD, LEFT_SHIFT, RIGHT_SHIFT,
        LESS, LESS_EQUAL, GREATER, GREATER_EQUAL,
        EQUAL, NOT_EQUAL, BIT_AND, BIT_XOR, BIT_OR,
        AND, OR;

        public static final ImmutableMap<String, BinaryOp> fromText = ImmutableMap.<String, BinaryOp>builder()
                .put("+", ADD)
                .put("-", SUB)
                .put("*", MUL)
                .put("/", DIV)
                .put("%", MOD)
                .put("<<", LEFT_SHIFT)
                .put(">>", RIGHT_SHIFT)
                .put("<", LESS)
                .put("<=", LESS_EQUAL)
                .put(">", GREATER)
                .put(">=", GREATER_EQUAL)
                .put("==", EQUAL)
                .put("!=", NOT_EQUAL)
                .put("&", BIT_AND)
                .put("^", BIT_XOR)
                .put("|", BIT_OR)
                .put("&&", AND)
                .put("||", OR)
                .build();
    }

    public enum ChanStatusOp {
        FULL, EMPTY, NFULL, NEMPTY;

        public static final Map<String, ChanStatusOp> from = ImmutableMap.<String, ChanStatusOp>builder()
                .put("full", FULL)
                .put("empty", EMPTY)
                .put("nfull", NFULL)
                .put("nempty", NEMPTY)
                .build();
    }

    public enum PredefVar {
        ELSE, TIMEOUT, PID, NR_PR;

        public static final Map<String, PredefVar> from = ImmutableMap.<String, PredefVar>builder()
                .put("else", ELSE)
                .put("timeout", TIMEOUT)
                .put("_pid", PID)
                .put("_nr_pr", NR_PR)
                .build();

        public static final Set<String> unsupported = ImmutableSet.<String>builder()
                .add("np_", "_last").build();
    }

    public enum UnaryOp {
        BIT_NOT, OPPOSITE_NUM, LOGIC_NOT;

        public static final ImmutableMap<String, UnaryOp> fromText = ImmutableMap.<String, UnaryOp>builder()
                .put("~", BIT_NOT)
                .put("-", OPPOSITE_NUM)
                .put("!", LOGIC_NOT)
                .build();
    }
}
