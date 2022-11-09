grammar SimpleExpr;

// *: 0 or more
prog : stat* EOF ;

// 'if': literal
stat : expr ';'
     | ID '=' expr ';'
     | 'if' expr ';'
     ;

// | : or
// (): subrule
expr : expr('*'|'/') expr
     | expr('+'|'-') expr
     | ID
     | INT
     ;

ID : (LETTER|'_') (LETTER|DIGIT|'_')* ;

LETTER : [a-zA-Z] ;
DIGIT : [0-9] ;