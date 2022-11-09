grammar SimpleExpr;

@header{
package simpleexpr;
}

// * : 0 or more
// + : 1 or more
prog : stat* EOF ;

// 'if': literal
stat : expr SEMI
     | ID ASSIGN expr SEMI
     | IF expr SEMI
     ;

// | : or
// (): subrule
expr : expr(MUL|DIV) expr
     | expr(ADD|SUB) expr
     | ID
     | INT
     ;

SEMI : ';' ;
ASSIGN : '=' ;
IF : 'if' ;
MUL : '*' ;
DIV : '/' ;
ADD : '+' ;
SUB : '-' ;


ID : (LETTER|'_') (LETTER|DIGIT|'_')* ;

INT : '0' | ([1-9] [0-9]*) ;

WS : [ \t\r\n]+ -> skip ;

// 不加？是贪婪模式，匹配所有符合条件的字符串
SL_COMMENT : '//' .*? '\n' -> skip ;
ML_COMMENT : '/*' .*? '*/' -> skip ;

fragment LETTER : [a-zA-Z] ;
fragment DIGIT : [0-9] ;

