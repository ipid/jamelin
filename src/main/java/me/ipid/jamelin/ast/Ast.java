package me.ipid.jamelin.ast;

import lombok.Data;

import java.util.List;
import java.util.Optional;

public class Ast {

    public interface AstAssignment extends AstStatement {
        AstVarRef getTarget();
    }

    public interface AstExpr extends AstNode {
    }

    public interface AstModule extends AstNode {
    }

    public interface AstNode {
    }

    public interface AstStatement extends AstNode {
    }

    public interface AstVarInit extends AstNode {
    }

    public interface AstVarRef extends AstNode {
    }

    public static final @Data
    class AstAdditionStatement implements AstAssignment {
        public final AstVarRef target;
        public final int addBy;
    }

    public static final @Data
    class AstArrayAccess implements AstVarRef {
        public final AstVarRef target;
        public final AstExpr index;
    }

    public static final @Data
    class AstBlockableStatement implements AstStatement {
        public final AstExpr expr;
    }

    public static final @Data
    class AstDeclare implements AstNode {
        public final boolean show, hidden, local;
        public final String typeName;
        public final int arrayLen;
        public final String varName;
        public final AstVarInit varInit;
    }

    public static final @Data
    class AstDeclareStatement implements AstStatement {
        public final List<AstDeclare> declares;
    }

    public static final @Data
    class AstMemberAccess implements AstVarRef {
        public final String member;
        public final AstVarRef target;
    }

    public static final @Data
    class AstMtype implements AstModule {
        public final List<String> ids;
        public final Optional<String> subType;
    }

    public static final @Data
    class AstPrintfStatement implements AstStatement {
        public final String template;
        public final List<AstExpr> parameters;
    }

    public static final @Data
    class AstProctype implements AstModule {
        public final boolean active;
        public final Optional<AstExpr> enabler;
        public final String name;
        public final List<AstDeclare> parameters;
        public final Optional<Integer> priority;
        public final AstStatementBlock statements;
    }

    public static final @Data
    class AstProgram implements AstNode {
        public final Optional<AstProctype> init;
        public final List<AstModule> modules;
    }

    public static final @Data
    class AstSetValueStatement implements AstAssignment {
        public final AstVarRef target;
        public final AstExpr value;
    }

    public static final @Data
    class AstStatementBlock implements AstStatement {
        public final List<AstStatement> statements;
    }

    public static final @Data
    class AstUnlessStatement implements AstStatement {
        public final AstStatement doThis, ifThisBlocking;
    }

    public static final @Data
    class AstUtype implements AstModule {
        public final List<AstDeclare> declares;
        public final String name;
    }

    public static final @Data
    class AstVarExprInit implements AstVarInit {
        public final AstExpr expr;
    }

    public static final @Data
    class AstVariableName implements AstVarRef {
        public final String varName;
    }

}
