package me.ipid.jamelin.constant;

import com.google.common.collect.ImmutableMap;

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
