package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.constant.*;
import me.ipid.jamelin.constant.PromelaLanguage.BinaryOp;
import me.ipid.jamelin.entity.*;
import me.ipid.jamelin.execute.*;

public class ILBinaryExpr implements ILExpr {

    private ILExpr left, right;
    private BinaryOp op;

    public ILBinaryExpr(ILExpr left, ILExpr right, BinaryOp op) {
        this.left = left;
        this.right = right;
        this.op = op;
    }

    @Override
    public int execute(JamelinKernel kernel, ProcessControlBlock procInfo) {
        int l = left.execute(kernel, procInfo), r = right.execute(kernel, procInfo);

        switch (op) {
            case ADD:
                return l + r;
            case SUB:
                return l - r;
            case MUL:
                return l * r;
            case DIV:
                return l / r;
            case MOD:
                return l % r;
            case LEFT_SHIFT:
                return l << r;
            case RIGHT_SHIFT:
                return l >> r;
            case LESS:
                return (l < r) ? 1 : 0;
            case LESS_EQUAL:
                return (l <= r) ? 1 : 0;
            case GREATER:
                return (l > r) ? 1 : 0;
            case GREATER_EQUAL:
                return (l >= r) ? 1 : 0;
            case EQUAL:
                return (l == r) ? 1 : 0;
            case NOT_EQUAL:
                return (l != r) ? 1 : 0;
            case BIT_AND:
                return l & r;
            case BIT_XOR:
                return l ^ r;
            case BIT_OR:
                return l | r;
            case AND:
                return (l != 0 && r != 0) ? 1 : 0;
            case OR:
                return (l != 0 || r != 0) ? 1 : 0;
        }
        throw new Error("Unreachable");
    }
}
