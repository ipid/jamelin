grammar Promela;

@lexer::members {
    // {}: 0, (): 1, []: 2
    private int bracketDepth[] = new int[3];

    private void newLeftBracket(int bracket) {
        bracketDepth[bracket]++;
    }

    private void newRightBracket(int bracket) {
        if (bracketDepth[bracket] > 0) {
            bracketDepth[bracket]--;
        }
    }

    private boolean shouldNewlineEmit() {
        // 在 {} 当中，且不在 () 与 [] 中
        return bracketDepth[0] > 0 && bracketDepth[1] <= 0 && bracketDepth[2] <= 0;
    }
}

spec
    : module* EOF
    ;

module
    : proctype
    | init
    | never
    | trace
    | utype
    | mtype
    | declareList
    ;

/* 注意：所有在 {} 内的规则都必须考虑 NEWLINE 的问题 */
proctype
    : active? PROCTYPE IDENTIFIER '(' declareList? ')' priority? enabler? '{' sequence '}'
    ;

init
    : INIT priority? '{' sequence '}'
    ;

never
    : NEVER '{' sequence '}'
    ;

trace
    : TRACE '{' sequence '}'
    ;

utype
    : TYPEDEF IDENTIFIER '{' declareList '}'
    ;

mtype
    : MTYPE '='? '{' (delimeter | ',' | IDENTIFIER)* '}'
    ;

// declareList 与 sequence 最好采用同样的逻辑，把 declareList 搞清就能搞清 sequence 使用尾后逗号（尾后逗号法）：尾后逗号指 trailing comma
declareList
    : delimeter? oneDeclare (delimeter oneDeclare)* delimeter?
    ;

// 本质：也是一条语句
oneDeclare
    : VISIBLE? typeName initVar (',' initVar)* ','?
    ;

active
    : ACTIVE ('[' constant ']')?
    ;

priority
    : PRIORITY constant
    ;

enabler
    : PROVIDED '(' expr ')'
    ;

sequence
    : delimeter? step (delimeter step)* delimeter?
    ;

step
    : statement (delimeter? UNLESS delimeter? statement)?
    ;

initVar
    : IDENTIFIER ('[' constant ']')? ('=' anyExpr | '=' chanInit)
    ;

chanInit
    : '[' constant ']' OF '{' (delimeter | ',' | typeName)
    ;

varRef
    : IDENTIFIER ( '[' anyExpr ']')? ('.' varRef)?
    ;

send
    : varRef '!' sendArgs | varRef '!' '!' sendArgs
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
    : argList | anyExpr '(' argList ')'
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

statement
    : IF choices FI
    | DO choices OD
    | FOR '(' range ')' '{' sequence '}'
    | ATOMIC '{' sequence '}'
    | D_STEP '{' sequence '}'
    | SELECT '(' range ')'
    | '{' sequence '}'
    | send
    | receive
    | assignment
//    | ELSE
    | BREAK
    | GOTO IDENTIFIER
    | IDENTIFIER ':' delimeter? statement
    | PRINTF '(' STRING (',' argList)? ')'
    | ASSERT expr
    | expr
    ;

range
    : varRef ':' expr '..' expr
    | varRef IN varRef
    ;

choices
    : '::' sequence ('::' sequence)*
    ;

anyExpr
    : '(' anyExpr ')'
    | anyExpr binaryOp anyExpr
    | UNARY_OP anyExpr
    | '(' anyExpr '->' anyExpr ':' anyExpr ')'
    | LEN '(' varRef ')'
    | poll
    | varRef
    | constant
    | TIMEOUT
    | NP_
    | ENABLED '(' anyExpr ')'
    | PC_VALUE '(' anyExpr ')'
    | IDENTIFIER '[' anyExpr ']' '@' IDENTIFIER
    | RUN IDENTIFIER '(' argList? ')' priority?
    | GET_PRIORITY '(' expr ')'
    | SET_PRIORITY '(' expr ',' expr ')'
    ;

// anyExpr 是 expr 的子集（建议改名为 someExpr）
expr
    : anyExpr
    | '(' expr ')'
    | expr ANDOR expr
    | CHAN_POLL '(' varRef ')'
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
    | CHAN
    | IDENTIFIER
    ;

constant
    : 'true' | 'false' | 'skip' | NUMBER
    ;

binaryOp
    : '+' | '-' | '*' | '/' | '%' | '&' | '^' | '|'
	| '>' | '<' | '>=' | '<=' | '==' | '!='
	| '<<' | '>>' | ANDOR;

delimeter
    : SIMPLE_DELIMETER | '->';

STRING
    : '"' ('\\"' | ~[\n\\]) '"'
    ;

/* Compound Keywords */
VISIBLE
    : 'show' | 'hidden'
    ;
CHAN_POLL
    : 'full' | 'empty' | 'nfull' | 'nempty'
    ;

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
C
    : 'c'
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
INT
    : 'int'
    ;
LEN
    : 'len'
    ;
MTYPE
    : 'mtype'
    ;
NEVER
    : 'never'
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
PRINTF
    : 'printf'
    ;
PRIORITY
    : 'priority'
    ;
PROCTYPE
    : 'proctype'
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
XR
    : 'xr'
    ;
XS
    : 'xs'
    ;

NUMBER
    : [0-9]+
    ;
IDENTIFIER
    : [a-zA-Z_] [a-zA-Z0-9_]*
    ;

ANDOR
    : '&&' | '||';

UNARY_OP
    : '~' | '-' | '!';

L_CURLY_BRACKET
    : '{' { newLeftBracket(0); }
    ;
R_CURLY_BRACKET
    : '}' { newRightBracket(0); }
    ;
L_PARENTHESIS
    : '(' { newLeftBracket(1); }
    ;
R_PARENTHESIS
    : ')' { newRightBracket(1); }
    ;
L_SQUARE_BRACKET
    : '[' { newLeftBracket(2); }
    ;
R_SQUARE_BRACKET
    : ']' { newRightBracket(2); }
    ;

SIMPLE_DELIMETER
    : ([;\n]) ([;\n \t\r])* { if (!shouldNewlineEmit()) { skip(); } }
    ;

/* Skips */
SKIP_WHITESPACE
    : [ \t\r]+ -> skip
    ;
SKIP_COMMENT_ONELINE
    : '//' (~'\n')* '\n' -> skip
    ;
SKIP_COMMENT_MULTILINE
    : '/*' .*? '*/' -> skip
    ;
