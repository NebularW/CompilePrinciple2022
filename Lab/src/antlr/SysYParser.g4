parser grammar SysYParser;

@header{
package antlr;
}

options {
    tokenVocab = SysYLexer;
}

program
   : compUnit
   ;
compUnit
   : (funcDef | decl)+ EOF
   ;
// 下面是其他的语法单元定义
decl
    : constDecl
    | varDecl
    ;

constDecl
    : CONST bType constDef (COMMA constDef)* SEMICOLON
    ;

bType : INT;

constDef
    : IDENT (L_BRACKT constExp R_BRACKT)* ASSIGN constInitVal
    ;

constInitVal
    : constExp
    | L_BRACE(constInitVal (COMMA constInitVal)* )? R_BRACE
    ;

varDecl
    : bType varDef (COMMA varDef)* SEMICOLON
    ;

varDef
    : IDENT (L_BRACKT constExp R_BRACKT)*
    | IDENT (L_BRACKT constExp R_BRACKT)* ASSIGN initVal
    ;

initVal
    : exp
    | L_BRACE (initVal (COMMA initVal)* )? R_BRACE
    ;

funcDef
    : funcType IDENT L_PAREN (funcFParams)? R_PAREN block
    ;

funcType
    : VOID
    | INT
    ;

funcFParams
    : funcFParam (COMMA funcFParam)*
    ;

funcFParam
    : bType IDENT (L_BRACKT R_BRACKT (L_BRACKT exp R_BRACKT)* )?
    ;

block
    : L_BRACE (blockItem)* R_BRACE
    ;

blockItem
    : decl
    | stmt
    ;

stmt
    : lVal ASSIGN exp SEMICOLON                     #stmt_assign
    | (exp)? SEMICOLON                              #stmt_judge
    | block                                         #stmt_block
    | IF L_PAREN cond R_PAREN stmt (ELSE stmt)?     #stmt_if
    | WHILE L_PAREN cond R_PAREN stmt               #stmt_while
    | BREAK SEMICOLON                               #stmt_break
    | CONTINUE SEMICOLON                            #stmt_continue
    | RETURN (exp)? SEMICOLON                       #stmt_return
    ;

exp
   : L_PAREN exp R_PAREN                    #exp_paren
   | lVal                                   #exp_lVal
   | number                                 #exp_number
   | IDENT L_PAREN funcRParams? R_PAREN     #exp_func
   | unaryOp exp                            #exp_unary
   | exp (MUL | DIV | MOD) exp              #exp_2
   | exp (PLUS | MINUS) exp                 #exp_1
   ;

cond
   : exp                                #cond_exp
   | cond (LT | GT | LE | GE) cond      #cond_1
   | cond (EQ | NEQ) cond               #cond_2
   | cond AND cond                      #cond_3
   | cond OR cond                       #cond_4
   ;

lVal
   : IDENT (L_BRACKT exp R_BRACKT)*
   ;

number
   : INTEGR_CONST
   ;

unaryOp
   : PLUS
   | MINUS
   | NOT
   ;

funcRParams
   : param (COMMA param)*
   ;

param
   : exp
   ;

constExp
   : exp
   ;
