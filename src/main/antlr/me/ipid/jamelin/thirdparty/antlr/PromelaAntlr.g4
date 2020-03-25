grammar PromelaAntlr;

@lexer::members {
    // (): 0, []: 1
    private int bracketDepth[] = new int[2];
    private boolean isDefiningFunction = false;

    private void newLeftBracket(int bracket) {
        bracketDepth[bracket]++;
    }

    private void newRightBracket(int bracket) {
        if (bracketDepth[bracket] > 0) {
            bracketDepth[bracket]--;
        }
    }

    private boolean shouldNewlineEmit() {
        return
            // Trying to define a `proctype`
            isDefiningFunction ||
            // Neither inside () or inside []
            (bracketDepth[0] <= 0 && bracketDepth[1] <= 0);
    }
}

spec
    : module+ EOF
    ;

// 加入了 inline 以支持 inline 语句块
// 加入 SIMPLE_DELIMETER，因为程序外面可能会有空行
module
    : proctype  # module_Proctype
    | init  # module_Init
    | never  # module_Never
    | trace  # module_Trace
    | utype  # module_Utype
    | mtype  # module_Mtype
    | declareList  # module_DeclareList
    | inline  # module_Inline
    | ltl  # module_Ltl
    | SIMPLE_DELIMETER  # module_Empty
    ;


proctype:
    (
        ACTIVE delimeter?
        ('[' constExpr ']')?
    )? delimeter?
    PROCTYPE delimeter?
    IDENTIFIER delimeter?
    '(' declareList? delimeter?
     ')' delimeter?
    priority? delimeter?
    enabler? delimeter?
    statementBlock;

init
    : INIT delimeter? priority? delimeter? statementBlock
    ;

never
    : NEVER delimeter? statementBlock
    ;

trace
    : (TRACE | NOTRACE) delimeter? statementBlock
    ;

utype
    : TYPEDEF delimeter? IDENTIFIER delimeter? '{' declareList '}'
    ;

mtype
    : MTYPE delimeter? (':' delimeter? subType=IDENTIFIER delimeter?)? '='? delimeter? '{' (delimeter | ',' | mtypeName+=IDENTIFIER )* '}'
    ;

inline:
    INLINE delimeter?
    IDENTIFIER delimeter?
    '(' (IDENTIFIER | ',')* ')' delimeter?
    statementBlock;

ltl:
    LTL delimeter?
    (IDENTIFIER delimeter?)?
    '{' .*? '}';

// declareList 与 sequence 最好采用同样的逻辑，把 declareList 搞清就能搞清 sequence
// 使用尾后逗号（尾后逗号法）：尾后逗号指 trailing comma
// diff：加入了尾后 delimeter
declareList
    : delimeter? oneDeclare (delimeter oneDeclare)* delimeter?
    ;

// 本质：也是一条语句
oneDeclare
    : (VISIBLE | LOCAL)? typeName initVar (',' initVar)*  # oneDeclare_Normal
    | VISIBLE? UNSIGNED IDENTIFIER ':' NUMBER initVar (',' IDENTIFIER ':' NUMBER initVar)*  # oneDeclare_Unsigned
    ;

initVar returns [String varName]
    : IDENTIFIER { $varName = $IDENTIFIER.text } '[' constExpr ']' '=' initializerList  # initVar_ArrayInitializerList
    | IDENTIFIER { $varName = $IDENTIFIER.text } '[' constExpr ']' '=' chanInit  # initVar_ArrayChanInit
    | IDENTIFIER { $varName = $IDENTIFIER.text } '[' constExpr ']' '=' anyExpr  # initVar_ArrayAnyExpr
    | IDENTIFIER { $varName = $IDENTIFIER.text } '[' constExpr ']'  # initVar_Array
    | IDENTIFIER { $varName = $IDENTIFIER.text } '=' anyExpr  # initVar_AnyExpr
    | IDENTIFIER { $varName = $IDENTIFIER.text } '=' chanInit  # initVar_ChanInit
    | IDENTIFIER { $varName = $IDENTIFIER.text }  # initVar_NoInit
    ;

initializerList
    : '{' delimeter? anyExpr 
            (delimeter? ',' delimeter? anyExpr)* delimeter?
            (',' delimeter?)? '}';

priority
    : PRIORITY delimeter? constExpr
    ;

enabler
    : PROVIDED delimeter? '(' expr ')'
    ;

sequence
    : delimeter? step (delimeter step)* delimeter?
    ;

step
    : statement delimeter? UNLESS delimeter? statement  # step_UnlessStatement
    | statement  # step_NormalStatement
    ;

chanInit
    : '[' constExpr ']' OF '{' (delimeter | ',' | typeName)* '}'
    ;

varRef
    : IDENTIFIER ('[' anyExpr ']')? ('.' varRef)?
    ;

send
    : varRef '!' sendArgs  # send_Fifo
    | varRef '!!' sendArgs  # send_Insert
    ;

receive
    : varRef '?' recvArgs  # receive_Fifo
    | varRef '??' recvArgs  # receive_Random
    | varRef '?' '<' recvArgs '>'  # receive_PollFifo
    | varRef '??' '<' recvArgs '>'  # receive_PollRandom
    ;

poll
    : varRef '?' '[' recvArgs ']'  # poll_Fifo
    | varRef '??' '[' recvArgs ']'  # poll_Random
    ;

sendArgs
    : argList  # sendArgs_Normal
    | anyExpr '(' argList ')'  # sendArgs_WithType
    ;

argList
    : anyExpr (',' anyExpr)* ','?
    ;

recvArgs
    : recvArgItem (',' recvArgItem)* ','?  # recvArgs_Normal
    | recvArgItem '(' recvArgs ')'  # recvArgs_WithBracket
    ;

recvArgItem
    : PREDEF_WRITEONLY  # recvArgItem_WriteOnly
    | varRef  # recvArgItem_VarRef
    | EVAL '(' anyExpr ')'  # recvArgItem_Eval
    | '-'? constExpr  # recvArgItem_Constant
    ;

assignment
    : varRef '=' anyExpr  # assignment_Normal
    | PREDEF_WRITEONLY '=' anyExpr  # assignment_Dummy
    | varRef '++'  # assignment_Increase
    | varRef '--'  # assignment_Decrease
    ;

statementBlock
    : '{' sequence '}'
    ;

// 把 step 里的一些东西挪到了这里
statement
    : oneDeclare  # statement_OneDeclare
    | XR varRef (',' varRef)*  # statement_Xr
    | XS varRef (',' varRef)*  # statement_Xs
    | IF choices FI  # statement_If
    | DO choices OD  # statement_Do
    | FOR delimeter? '(' range ')' delimeter? statementBlock  # statement_For
    | ATOMIC delimeter? statementBlock  # statement_Atomic
    | D_STEP delimeter? statementBlock  # statement_Dstep
    | SELECT delimeter? '(' varRef ':' expr '..' expr ')'  # statement_Select
    | statementBlock  # statement_Compound
// 这里把 else 放到表达式里了
    | BREAK  # statement_Break
    | GOTO delimeter? IDENTIFIER  # statement_Goto
    | IDENTIFIER ':' delimeter? statement  # statement_Labeled
    | PRINTF delimeter? '(' STRING (',' argList)? ')'  # statement_Printf
    | PRINTM delimeter? '(' varRef ')'  # statement_Printm
    | ASSERT delimeter? expr  # statement_Assert
    | expr  # statement_Expr
    | send  # statement_Send
    | receive  # statement_Receive
    | assignment  # statement_Assign
    | IDENTIFIER '(' ( expr (',' expr)* ','? )? ')'  # statement_CallInline
    ;

range
    : varRef ':' expr '..' expr  # range_Numeric
    | varRef IN varRef  # range_Iterate
    ;

choices
    : delimeter? '::' sequence ('::' sequence)*
    ;

anyExpr
    : constExpr  # anyExpr_Constant
    | '(' anyExpr ')'  # anyExpr_Compound
    | <assoc=right> ('~' | '-' | '!') delimeter? anyExpr  # anyExpr_Unary
    | anyExpr ('*' | '/' | '%') delimeter? anyExpr  # anyExpr_Binary
    | anyExpr ('+' | '-') delimeter? anyExpr  # anyExpr_Binary
    | anyExpr ('<<' | '>>') delimeter? anyExpr  # anyExpr_Binary
    | anyExpr ('<' | '<=' | '>' | '>=') delimeter? anyExpr  # anyExpr_Binary
    | anyExpr ('==' | '!=') delimeter? anyExpr  # anyExpr_Binary
    | anyExpr '&' delimeter? anyExpr  # anyExpr_Binary
    | anyExpr '^' delimeter? anyExpr  # anyExpr_Binary
    | anyExpr '|' delimeter? anyExpr  # anyExpr_Binary
    | anyExpr '&&' delimeter? anyExpr  # anyExpr_Binary
    | anyExpr '||' delimeter? anyExpr  # anyExpr_Binary
    | <assoc=right> '(' anyExpr '->' anyExpr ':' anyExpr ')'  # anyExpr_Ternary
    | LEN '(' varRef ')'  # anyExpr_Len
    | poll  # anyExpr_Poll
    | varRef  # anyExpr_VarRef
    | ELSE  # anyExpr_PredefVar
    | TIMEOUT  # anyExpr_PredefVar
    | NP_  # anyExpr_PredefVar
    | PREDEF_PID  # anyExpr_PredefVar
    | PREDEF_LAST  # anyExpr_PredefVar
    | PREDEF_NR_PR  # anyExpr_PredefVar
    | ENABLED '(' anyExpr ')'  # anyExpr_Enabled
    | PC_VALUE '(' anyExpr ')'  # anyExpr_PcValue
    | IDENTIFIER '[' anyExpr ']' '@' IDENTIFIER  # anyExpr_RemoteRef
    | RUN IDENTIFIER '(' argList? ')' priority?  # anyExpr_Run
    | GET_PRIORITY '(' expr ')'  # anyExpr_GetPriority
    | SET_PRIORITY '(' expr ',' expr ')'  # anyExpr_SetPriority
    ;

// anyExpr 是 expr 的子集；anyExpr 比 expr 少了一个 CHAN_POLL（建议改名为 someExpr）
expr
    : anyExpr  # expr_AnyExpr
    | '(' expr ')'  # expr_Compound
    | expr '&&' delimeter? expr  # expr_Binary
    | expr '||' delimeter? expr  # expr_Binary
    | CHAN_POLL '(' varRef ')'  # expr_ChanPoll
    ;

constExpr
    : '(' constExpr ')'  # constExpr_Compound
    | <assoc=right> '-' delimeter? constExpr  # constExpr_Unary
    | constExpr '*' delimeter? constExpr  # constExpr_Binary
    | constExpr ('+' | '-') delimeter? constExpr  # constExpr_Binary
    | TRUE_SKIP  # constExpr_True
    | FALSE  # constExpr_False
    | NUMBER  # constExpr_Number
    | CHAR_LITERAL  # constExpr_CharLiteral
    ;

/* 几乎相当于词组的语法规则 */
typeName
    : BIT
    | BOOL
    | BYTE
    | CHAN
    | IDENTIFIER
    | INT
    | MTYPE
    | MTYPE ':' IDENTIFIER
    | PID
    | SHORT
    ;

delimeter
    : (SIMPLE_DELIMETER | '->')+;

/* 复合关键字 */
VISIBLE
    : 'show' | 'hidden'
    ;
CHAN_POLL
    : 'full' | 'empty' | 'nfull' | 'nempty'
    ;
TRUE_SKIP
    : 'true' | 'skip';

/* 关键字（自动生成） */
ACTIVE
    : 'active'
    ;
ASSERT
    : 'assert'
    ;
ATOMIC
    : 'atomic'
    ;
BIT
    : 'bit'
    ;
BOOL
    : 'bool'
    ;
BREAK
    : 'break'
    ;
BYTE
    : 'byte'
    ;
CHAN
    : 'chan'
    ;
DO
    : 'do'
    ;
D_STEP
    : 'd_step'
    ;
ELSE
    : 'else'
    ;
ENABLED
    : 'enabled'
    ;
EVAL
    : 'eval'
    ;
FALSE
    : 'false'
    ;
FI
    : 'fi'
    ;
FOR
    : 'for'
    ;
GET_PRIORITY
    : 'get_priority'
    ;
GOTO
    : 'goto'
    ;
IF
    : 'if'
    ;
IN
    : 'in'
    ;
INIT
    : 'init'
    ;
INLINE
    : 'inline'
    ;
INT
    : 'int'
    ;
LEN
    : 'len'
    ;
LOCAL
    : 'local'
    ;
LTL
    : 'ltl'
    ;
MTYPE
    : 'mtype'
    ;
NEVER
    : 'never'
    ;
NOTRACE
    : 'notrace'
    ;
NP_
    : 'np_'
    ;
OD
    : 'od'
    ;
OF
    : 'of'
    ;
PC_VALUE
    : 'pc_value'
    ;
PID
    : 'pid'
    ;
PREDEF_LAST
    : '_last'
    ;
PREDEF_NR_PR
    : '_nr_pr'
    ;
PREDEF_PID
    : '_pid'
    ;
PREDEF_WRITEONLY
    : '_'
    ;
PRINTF
    : 'printf'
    ;
PRINTM
    : 'printm'
    ;
PRIORITY
    : 'priority'
    ;
PROVIDED
    : 'provided'
    ;
RUN
    : 'run'
    ;
SELECT
    : 'select'
    ;
SET_PRIORITY
    : 'set_priority'
    ;
SHORT
    : 'short'
    ;
TIMEOUT
    : 'timeout'
    ;
TRACE
    : 'trace'
    ;
TYPEDEF
    : 'typedef'
    ;
UNLESS
    : 'unless'
    ;
UNSIGNED
    : 'unsigned'
    ;
XR
    : 'xr'
    ;
XS
    : 'xs'
    ;

/* Special keywords with actions */
PROCTYPE
    : 'proctype' { isDefiningFunction = true; }
    ;

NUMBER
    : [0-9]+
    ;
IDENTIFIER
    : [a-zA-Z_] [a-zA-Z0-9_]*
    ;

/* 特殊符号 */
STRING
    : '"' ('\\' . | .)*? '"'
    ;
CHAR_LITERAL
    : '\'' ('\\' . | .) '\''
    ;

PUNC_COMMA: ',';
PUNC_EQUAL: '=';
PUNC_COLON: ':';

L_PARENTHESIS
    : '(' { newLeftBracket(0); }
    ;
R_PARENTHESIS
    : ')' { newRightBracket(0); }
    ;
L_SQUARE_BRACKET
    : '[' { newLeftBracket(1); }
    ;
R_SQUARE_BRACKET
    : ']' { newRightBracket(1); }
    ;
L_CURLY_BRACKET
    : '{' { isDefiningFunction = false; }
    ;

SIMPLE_DELIMETER
    : ([;\n]) ([;\n \t\r])* { if (!shouldNewlineEmit()) { skip(); } }
    ;

/* LTL Symbols */
LTL_BACKSLASH: '\\';

/* Skips */
SKIP_WHITESPACE
    : [ \t\r]+ -> skip
    ;
SKIP_COMMENT_ONELINE
    : '//' .*? '\n' -> skip
    ;
SKIP_COMMENT_MULTILINE
    : '/*' .*? '*/' -> skip
    ;
