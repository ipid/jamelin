package me.ipid.jamelin.ast;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.ipid.jamelin.constant.PromelaLanguage.ChanStatusOp;
import me.ipid.jamelin.constant.PromelaLanguage.PredefVar;

import java.util.List;
import java.util.Optional;

public class Ast {

    public interface AstAssignment extends AstStatement {
        AstVarRef getTarget();
    }

    public interface AstDeclare {
        String getTypeName();

        Optional<AstVarInit> getVarInit();

        String getVarName();
    }

    public interface AstExpr extends AstNode {
    }

    public interface AstNode {
        default boolean isDummy() {
            return false;
        }
    }

    public interface AstRecvArgItem extends AstNode {
    }

    public interface AstStatement extends AstNode {
    }

    public interface AstVarInit extends AstNode {
    }

    public interface AstVarRef extends AstNode {
    }

    public static final @RequiredArgsConstructor
    class AstAdditionStatement implements AstAssignment {
        @Getter
        public final @NonNull AstVarRef target;

        public final int addBy;
    }

    public static final @RequiredArgsConstructor
    class AstArrayAccess implements AstVarRef {
        public final @NonNull AstVarRef target;
        public final @NonNull AstExpr index;
    }

    public static final @RequiredArgsConstructor
    class AstAssertStatement implements AstStatement {
        public final @NonNull AstExpr toBeTrue;
    }

    public static final @RequiredArgsConstructor
    class AstAtomicStatement implements AstStatement {
        public final @NonNull List<AstStatement> statements;
        public final boolean dStep;
    }

    public static final @RequiredArgsConstructor
    class AstBinaryExpr implements AstExpr {
        public final @NonNull String op;
        public final @NonNull AstExpr a, b;
    }

    public static final @RequiredArgsConstructor
    class AstBlockableStatement implements AstStatement {
        public final @NonNull AstExpr expr;
    }

    public static final @RequiredArgsConstructor
    class AstBreakStatement implements AstStatement {
    }

    public static final @RequiredArgsConstructor
    class AstCallInlineStatement implements AstStatement {
        public final @NonNull String target;
        public final @NonNull List<AstExpr> args;
    }

    public static final @RequiredArgsConstructor
    class AstChanInit implements AstVarInit {
        public final int bufLen;
        public final @NonNull List<String> elemTypeTuple;
    }

    public static final @RequiredArgsConstructor
    class AstChanPollExpr implements AstExpr {
        public final @NonNull AstVarRef chan;
        public final @NonNull List<AstRecvArgItem> args;
        public final boolean fifo;
    }

    public static final @RequiredArgsConstructor
    class AstChanStatusExpr implements AstExpr {
        public final @NonNull ChanStatusOp op;
        public final @NonNull AstVarRef chan;
    }

    public static final @RequiredArgsConstructor
    class AstConstExpr implements AstExpr {
        public final int num;
    }

    public static final @RequiredArgsConstructor
    class AstConstRecvArg implements AstRecvArgItem {
        public final int num;
    }

    public static final @RequiredArgsConstructor
    class AstDeclareStatement implements AstStatement {
        public final @NonNull List<AstDeclare> declares;
    }

    public static final @RequiredArgsConstructor
    class AstEvalExprRecvArg implements AstRecvArgItem {
        public final @NonNull AstExpr expr;
    }

    public static final @RequiredArgsConstructor
    class AstExprAsInit implements AstVarInit {
        public final @NonNull AstExpr expr;
    }

    public static final @RequiredArgsConstructor
    class AstForInStatement implements AstStatement {
        public final @NonNull AstVarRef storeTo, iterateFrom;
        public final @NonNull List<AstStatement> statements;
    }

    public static final @RequiredArgsConstructor
    class AstForStatement implements AstStatement {
        public final @NonNull AstVarRef storeTo;
        public final @NonNull AstExpr lowerLimit, upperLimit;
        public final @NonNull List<AstStatement> statements;
    }

    public static final @RequiredArgsConstructor
    class AstGotoStatement implements AstStatement {
        public final @NonNull String toLabel;
    }

    public static final @RequiredArgsConstructor
    class AstIfDoStatement implements AstStatement {
        public final @NonNull List<List<AstStatement>> choices;

        // true / false 表示当前为 do / if 语句
        public final boolean isDo;
    }

    public static final @RequiredArgsConstructor
    class AstInitializerList implements AstVarInit {
        public final @NonNull List<AstExpr> exprs;
    }

    public static final @RequiredArgsConstructor
    class AstInline implements AstNode {
        public final @NonNull String name;
        public final @NonNull List<String> args;
        public final @NonNull List<AstStatement> statements;
    }

    public static final @RequiredArgsConstructor
    class AstLabeledStatement implements AstStatement {
        public final @NonNull String label;
        public final @NonNull AstStatement target;
    }

    public static final @RequiredArgsConstructor
    class AstLenExpr implements AstExpr {
        public final @NonNull AstVarRef target;
    }

    public static final @RequiredArgsConstructor
    class AstMemberAccess implements AstVarRef {
        public final @NonNull AstVarRef target;
        public final @NonNull String member;
    }

    public static final @RequiredArgsConstructor
    class AstMtype {
        public final @NonNull List<String> ids;
        public final @NonNull Optional<String> subType;
    }

    public static final @RequiredArgsConstructor
    class AstNormalDeclare implements AstDeclare {
        public final boolean show, hidden, local;

        @Getter
        public final @NonNull String typeName;

        public final int arrayLen;

        @Getter
        public final @NonNull String varName;

        @Getter
        public final @NonNull Optional<AstVarInit> varInit;
    }

    public static final @RequiredArgsConstructor
    class AstPredefVarExpr implements AstExpr {
        public final @NonNull PredefVar predef;
    }

    public static final @RequiredArgsConstructor
    class AstPrintfStatement implements AstStatement {
        public final @NonNull String template;
        public final @NonNull List<AstExpr> args;
    }

    public static final @RequiredArgsConstructor
    class AstProctype implements AstNode {
        public final boolean active;
        public final @NonNull Optional<AstExpr> enabler;
        public final @NonNull String name;
        public final @NonNull List<AstDeclare> args;
        public final @NonNull Optional<Integer> priority;
        public final @NonNull List<AstStatement> statements;
    }

    public static final @RequiredArgsConstructor
    class AstProgram {
        public final @NonNull List<AstProctype> procs;
        public final @NonNull List<AstUtype> utypes;
        public final @NonNull List<AstMtype> mtypes;
        public final @NonNull List<AstDeclare> declares;
        public final @NonNull List<AstInline> inlines;
        public final @NonNull Optional<AstProctype> init;
    }

    public static final @RequiredArgsConstructor
    class AstRecvStatement implements AstStatement {
        public final @NonNull AstVarRef chan;
        public final @NonNull List<AstRecvArgItem> args;

        public final boolean peek, fifo;
    }

    public static final @RequiredArgsConstructor
    class AstRunExpr implements AstExpr {
        public final @NonNull String procName;
        public final @NonNull List<AstExpr> args;
        public final @NonNull Optional<Integer> priority;
    }

    public static final @RequiredArgsConstructor
    class AstSelectStatement implements AstStatement {
        public final @NonNull AstVarRef target;
        public final @NonNull AstExpr lowerLimit, upperLimit;
    }

    public static final @RequiredArgsConstructor
    class AstSendStatement implements AstStatement {
        public final @NonNull AstVarRef chan;
        public final @NonNull List<AstExpr> args;
        public final boolean fifo;
    }

    public static final @RequiredArgsConstructor
    class AstSetPid implements AstExpr {
        public final @NonNull AstExpr pid, setTo;
    }

    public static final @RequiredArgsConstructor
    class AstSetValueStatement implements AstAssignment {
        @Getter
        public final @NonNull AstVarRef target;

        public final @NonNull AstExpr value;
    }

    public static final @RequiredArgsConstructor
    class AstStatementsBlock implements AstStatement {
        public final @NonNull List<AstStatement> statements;
    }

    public static final @RequiredArgsConstructor
    class AstTernaryExpr implements AstExpr {
        public final @NonNull AstExpr cond, ifTrue, ifFalse;
    }

    public static final @RequiredArgsConstructor
    class AstUnaryExpr implements AstExpr {
        public final @NonNull String op;
        public final @NonNull AstExpr target;
    }

    public static final @RequiredArgsConstructor
    class AstUnlessStatement implements AstStatement {
        public final @NonNull AstStatement doThis, ifThisBlocking;
    }

    public static final @RequiredArgsConstructor
    class AstUnsignedDeclare implements AstDeclare {
        @Getter
        public final @NonNull String varName;
        public final int bitLen;

        @Getter
        public final @NonNull Optional<AstVarInit> varInit;

        public String getTypeName() {
            return "unsigned";
        }
    }

    public static final @RequiredArgsConstructor
    class AstUtype {
        public final @NonNull String newTypeName;
        public final @NonNull List<AstDeclare> members;
    }

    public static final @RequiredArgsConstructor
    class AstVarNameAccess implements AstVarRef {
        public final @NonNull String varName;
    }

    public static final @RequiredArgsConstructor
    class AstVarRefExpr implements AstExpr {
        public final @NonNull AstVarRef vRef;
    }

    public static final @RequiredArgsConstructor
    class AstVarRefRecvArg implements AstRecvArgItem {
        public final @NonNull AstVarRef target;
    }

    public static final @RequiredArgsConstructor
    class AstWriteOnlyRecvArg implements AstRecvArgItem {
    }

}
