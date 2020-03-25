package me.ipid.jamelin.ast;

import java.util.List;
import java.util.Optional;

public class Ast {

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

    public static class AstArrayAccess implements AstVarRef {

    }

    public static class AstAssignStatement implements AstStatement {

    }

    public static class AstBlockableStatement implements AstStatement {
        public final AstExpr expr;

        public AstBlockableStatement(AstExpr expr) {
            this.expr = expr;
        }
    }

    public static class AstDeclare implements AstNode {
        public final int arrayLen;
        public final boolean show, hidden, local;
        public final String typeName, varName;
        public final AstVarInit varInit;

        public AstDeclare(boolean show, boolean hidden, boolean local, String typeName,
                          String varName, int arrayLen, AstVarInit varInit) {
            this.show = show;
            this.hidden = hidden;
            this.local = local;
            this.typeName = typeName;
            this.varName = varName;
            this.arrayLen = arrayLen;
            this.varInit = varInit;
        }
    }

    public static class AstDeclareStatement implements AstStatement {

    }

    public static class AstDummy implements AstNode {
    }

    public static class AstDummyStatement implements AstStatement {
    }

    public static class AstMemberAccess implements AstVarRef {

    }

    public static class AstMtype implements AstModule {
        public final List<String> ids;
        public final Optional<String> subType;

        public AstMtype(Optional<String> subType, List<String> ids) {
            this.subType = subType;
            this.ids = ids;
        }
    }

    public static class AstPrintfStatement implements AstStatement {
        public final List<AstExpr> parameters;
        public final String template;

        public AstPrintfStatement(String template, List<AstExpr> parameters) {
            this.template = template;
            this.parameters = parameters;
        }
    }

    public static class AstProctype implements AstModule {
        public final boolean active;
        public final Optional<AstExpr> enabler;
        public final String name;
        public final List<AstDeclare> parameters;
        public final Optional<Integer> priority;
        public final AstStatementBlock statements;

        public AstProctype(
                boolean active, String name, List<AstDeclare> parameters,
                Optional<Integer> priority, Optional<AstExpr> enabler, AstStatementBlock statements) {
            this.active = active;
            this.name = name;
            this.parameters = parameters;
            this.priority = priority;
            this.enabler = enabler;
            this.statements = statements;
        }
    }

    public static class AstProgram implements AstNode {
        public final Optional<AstProctype> init;
        public final List<AstModule> modules;

        public AstProgram(List<AstModule> modules, Optional<AstProctype> init) {
            this.modules = modules;
            this.init = init;
        }
    }

    public static class AstStatementBlock implements AstStatement {
        public final List<AstStatement> statements;

        public AstStatementBlock(List<AstStatement> statements) {
            this.statements = statements;
        }
    }

    public static class AstUnlessStatement implements AstStatement {
        public final AstStatement doThis, ifThisBlocking;

        public AstUnlessStatement(AstStatement doThis, AstStatement ifThisBlocking) {
            this.doThis = doThis;
            this.ifThisBlocking = ifThisBlocking;
        }
    }

    public static class AstUtype implements AstModule {
        public final List<AstDeclare> declares;
        public final String name;

        public AstUtype(String name, List<AstDeclare> declares) {
            this.name = name;
            this.declares = declares;
        }
    }

    public static class AstVarExprInit implements AstVarInit {
        public final AstExpr expr;

        public AstVarExprInit(AstExpr expr) {
            this.expr = expr;
        }
    }

}
