grammar Promela;

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
    : proctype # MODULE_PROCTYPE
    | init  # MODULE_INIT
    | never  # MODULE_NEVER
    | trace  # MODULE_TRACE
    | utype  # MODULE_UTYPE
    | mtype  # MODULE_MTYPE
    | declareList  # MODULE_DECLARE
    | inline  # MODULE_INLINE
    | ltl  # MODULE_LTL
    | SIMPLE_DELIMETER  # MODULE_EMPTY
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
    '{' sequence '}';

init
    : INIT delimeter? priority? delimeter? '{' sequence '}'
    ;

never
    : NEVER delimeter? '{' sequence '}'
    ;

trace
    : (TRACE | NOTRACE) delimeter? '{' sequence '}'
    ;

utype
    : TYPEDEF delimeter? IDENTIFIER delimeter? '{' declareList '}'
    ;

mtype
    : MTYPE delimeter? (':' delimeter? IDENTIFIER delimeter?)? '='? delimeter? '{' (delimeter | ',' | IDENTIFIER)* '}'
    ;

inline:
    INLINE delimeter?
    IDENTIFIER delimeter?
    '(' (IDENTIFIER | ',')* ')' delimeter?
    '{' sequence '}';

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
    : (VISIBLE | LOCAL)? typeName IDENTIFIER initVar (',' IDENTIFIER initVar)*
    | VISIBLE? UNSIGNED IDENTIFIER ':' NUMBER initVar (',' IDENTIFIER ':' NUMBER initVar)*
    ;

priority
    : PRIORITY delimeter? constant
    ;

enabler
    : PROVIDED delimeter? '(' expr ')'
    ;

// 这里把 visible 移到下面去了

sequence
    : delimeter? step (delimeter step)* delimeter?
    ;

step
    : statement (delimeter? UNLESS delimeter? statement)?
    ;

initVar
    : ('[' constExpr ']')? ('=' anyExpr | '=' chanInit)?
    ;

chanInit
    : '[' constExpr ']' OF '{' (delimeter | ',' | typeName)* '}'
    ;

varRef
    : IDENTIFIER ('[' anyExpr ']')? ('.' varRef)?
    ;

send
    : varRef '!' sendArgs
    | varRef '!!' sendArgs
    ;

receive
    : varRef '?' recvArgs
    | varRef '??' recvArgs
    | varRef '?' '<' recvArgs '>'
    | varRef '??' '<' recvArgs '>'
    ;

poll
    : varRef '?' '[' recvArgs ']'
    | varRef '??' '[' recvArgs ']'
    ;

sendArgs
    : argList
    | anyExpr '(' argList ')'
    ;

argList
    : anyExpr (',' anyExpr)* ','?
    ;

recvArgs
    : recvArgItem (',' recvArgItem)* ','?
    | recvArgItem '(' recvArgs ')'
    ;

recvArgItem
    : varRef
    | EVAL '(' varRef ')'
    | '-'? constant
    ;

assignment
    : varRef '=' anyExpr
    | varRef '++'
    | varRef '--'
    ;

// 把 step 里的一些东西挪到了这里
statement
    : oneDeclare
    | XR varRef (',' varRef)*
    | XS varRef (',' varRef)*
    | IF choices FI
    | DO choices OD
    | FOR delimeter? '(' range ')' delimeter? '{' sequence '}'
    | ATOMIC delimeter? '{' sequence '}'
    | D_STEP delimeter? '{' sequence '}'
    | SELECT delimeter? '(' varRef ':' expr '..' expr ')'
    | '{' sequence '}'
// 这里把 else 放到表达式里了
    | BREAK
    | GOTO IDENTIFIER
    | IDENTIFIER ':' delimeter? statement
    | PRINTF delimeter? '(' STRING (',' argList)? ')'
    | PRINTM delimeter? '(' varRef ')'
    | ASSERT delimeter? expr
    | expr
    | send
    | receive
    | assignment
    | IDENTIFIER '(' ( expr (',' expr)* ','? )? ')' // 内联调用
    ;

range
    : varRef ':' expr '..' expr
    | varRef IN varRef
    ;

choices
    : delimeter? '::' sequence ('::' sequence)*
    ;

anyExpr
    : '(' anyExpr ')'
    | <assoc=right> ('~' | '-' | '!') delimeter? anyExpr
    | anyExpr ('*' | '/' | '%') delimeter? anyExpr
    | anyExpr ('+' | '-') delimeter? anyExpr
    | anyExpr ('<<' | '>>') delimeter? anyExpr
    | anyExpr ('<' | '<=' | '>' | '>=') delimeter? anyExpr
    | anyExpr ('==' | '!=') delimeter? anyExpr
    | anyExpr '&' delimeter? anyExpr
    | anyExpr '^' delimeter? anyExpr
    | anyExpr '|' delimeter? anyExpr
    | anyExpr '&&' delimeter? anyExpr
    | anyExpr '||' delimeter? anyExpr
    | <assoc=right> '(' anyExpr '->' anyExpr ':' anyExpr ')'
    | LEN '(' varRef ')'
    | poll
    | varRef
    | constant
    | ELSE
    | TIMEOUT
    | NP_
    | ENABLED '(' anyExpr ')'
    | PC_VALUE '(' anyExpr ')'
    | IDENTIFIER '[' anyExpr ']' '@' IDENTIFIER
    | RUN IDENTIFIER '(' argList? ')' priority?
    | GET_PRIORITY '(' expr ')'
    | SET_PRIORITY '(' expr ',' expr ')'
    ;

// anyExpr 是 expr 的子集；anyExpr 比 expr 少了一个 CHAN_POLL（建议改名为 someExpr）
expr
    : anyExpr
    | '(' expr ')'
    | expr '&&' delimeter? expr
    | expr '||' delimeter? expr
    | CHAN_POLL '(' varRef ')'
    ;

constExpr
    : '(' constExpr ')'
    | <assoc=right> '-' constExpr
    | constExpr '*' constExpr
    | constExpr ('+' | '-') constExpr
    | constant
    ;

// TODO: 把 else 作为表达式

/* 几乎相当于词组的语法规则 */
typeName
    : BIT
    | BOOL
    | BYTE
    | SHORT
    | INT
    | MTYPE
    | MTYPE ':' IDENTIFIER
    | CHAN
    | IDENTIFIER
    ;

constant
    : TRUE_FALSE_SKIP | NUMBER | CHAR_LITERAL
    ;

delimeter
    : (SIMPLE_DELIMETER | '->')+;

STRING
    : '"' ('\\' . | .)*? '"'
    ;
CHAR_LITERAL
    : '\'' ('\\' . | .) '\''
    ;

/* Compound Keywords */
VISIBLE
    : 'show' | 'hidden'
    ;
CHAN_POLL
    : 'full' | 'empty' | 'nfull' | 'nempty'
    ;
TRUE_FALSE_SKIP
    : 'true' | 'false' | 'skip';

/* Keywords */
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
PRINT
    : 'print'
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
