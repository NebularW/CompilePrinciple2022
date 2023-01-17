package symtable;

import antlr.SysYParser;
import antlr.SysYParserBaseVisitor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.List;

public class SymbolTableVisitor extends SysYParserBaseVisitor<Type> {
    private GlobalScope globalScope = null;
    private Scope currentScope = null;
    private int localScopeCounter = 0;
    public boolean error = false;

    @Override
    public Type visitProgram(SysYParser.ProgramContext ctx) {
        globalScope = new GlobalScope(null);
        currentScope = globalScope;
        Type type = super.visitProgram(ctx);
        currentScope = currentScope.getEnclosingScope();
        return type;
    }

    @Override
    public Type visitFuncDef(SysYParser.FuncDefContext ctx) {
        String funcName = ctx.IDENT().getText();
        if (currentScope.resolve(funcName, true) != null) {
            error = true;
            System.err.println("Error type 4 at Line " + ctx.getStart().getLine() + ": Redefined function: " + funcName);
        } else {
            Type returnType = new BasicTypeSymbol(ctx.funcType().getText());
            List<Type> paramTypes = new ArrayList<>();
            FuncType type = new FuncType(returnType, paramTypes);
            FunctionSymbol funcSymbol = new FunctionSymbol(funcName, currentScope, type);
            currentScope.define(funcSymbol);
            currentScope = funcSymbol;
            if (ctx.funcFParams() == null || visit(ctx.funcFParams()) == null) {
                visit(ctx.block());
            }
            currentScope = currentScope.getEnclosingScope();
        }
        return null;
    }

    @Override
    public Type visitFuncFParams(SysYParser.FuncFParamsContext ctx) {
        List<Type> fParamList = new ArrayList<>();
        for (SysYParser.FuncFParamContext param : ctx.funcFParam()) {
            String varName = param.IDENT().getText();
            // 变量重名

            if (currentScope.resolve(varName, true) != null) {
                Token token = ctx.getStart();
                error = true;
                System.err.println("Error type 3 at Line " + token.getLine() + ": Redefined variable: " + varName);
            } else {
                String text = param.getText();
                Type type;
                if (text.contains("]")) {
                    int dimension = param.L_BRACKT().size();
                    type = new ArrayType(dimension);
                } else {
                    type = new BasicTypeSymbol("int");
                }
                VariableSymbol var = new VariableSymbol(varName, type);
                fParamList.add(type);
                currentScope.define(var);
            }
        }
        try {
            FunctionSymbol functionSymbol = (FunctionSymbol) currentScope;
            FuncType type = functionSymbol.getType();
            type.paramsType = fParamList;
            functionSymbol.setType(type);
        } catch (Exception e) {
            System.out.println("Class Cast Exception");
        }
        return null;
    }

    @Override
    public Type visitBlock(SysYParser.BlockContext ctx) {
        if (ctx.getParent().getRuleIndex() != 10) {
            LocalScope localScope = new LocalScope(currentScope);
            String localScopeName = localScope.getName() + localScopeCounter;
            localScope.setName(localScopeName);
            localScopeCounter++;
            currentScope = localScope;
            visitChildren(ctx);
            currentScope = currentScope.getEnclosingScope();
        } else {
            visitChildren(ctx);
        }
        return null;
    }

    @Override
    public Type visitConstDecl(SysYParser.ConstDeclContext ctx) {
        String text = getFullText(ctx);
        for (SysYParser.ConstDefContext defCtx : ctx.constDef()) {
            String varName = defCtx.IDENT().getText();
            String location = ctx.getStart().getLine() + " " + getColumn(text, varName);
            boolean type3Error = defineVar(varName, defCtx.getStart(), defCtx.getText());
            if (!type3Error && defCtx.constInitVal() != null && defCtx.constInitVal().constExp() != null) {
                Type type = visit(defCtx.constInitVal().constExp());
                if (type.toString().equals("int")) {
                    Symbol var = currentScope.resolve(varName, true);
                    var.setType(new BasicType("int"));
                }
                if (!type.toString().equals("error") && !type.toString().equals("int")) {
                    if (type.toString().equals("func")) {
                        FuncType funcType = (FuncType) type;
                        if (!funcType.returnType.toString().equals("int")) {
                            error = true;
                            System.err.println("Error type 5 at Line " + ctx.getStart().getLine() + ": type.Type mismatched for assignment.");
                        }
                    } else {
                        error = true;
                        System.err.println("Error type 5 at Line " + ctx.getStart().getLine() + ": type.Type mismatched for assignment.");
                    }
                }
            }
//            System.out.println(location);
        }
        return null;
    }

    @Override
    public Type visitVarDecl(SysYParser.VarDeclContext ctx) {
        String text = getFullText(ctx);
        for (SysYParser.VarDefContext defCtx : ctx.varDef()) {
            String varName = defCtx.IDENT().getText();
            String location = ctx.getStart().getLine() + " " + getColumn(text, varName);
            boolean type3Error = defineVar(varName, defCtx.getStart(), defCtx.getText());
            if (!type3Error && defCtx.initVal() != null && defCtx.initVal().exp() != null) {
                Type type = visit(defCtx.initVal().exp());
                if (type.toString().equals("int")) {
                    Symbol var = currentScope.resolve(varName, true);
                    var.setType(new BasicType("int"));
                }
                if (!type.toString().equals("error") && !type.toString().equals("int")) {
                    if (type.toString().equals("func")) {
                        FuncType funcType = (FuncType) type;
                        if (!funcType.returnType.toString().equals("int")) {
                            error = true;
                            System.err.println("Error type 5 at Line " + ctx.getStart().getLine() + ": type.Type mismatched for assignment.");
                        }
                    } else {
                        error = true;
                        System.err.println("Error type 5 at Line " + ctx.getStart().getLine() + ": type.Type mismatched for assignment.");
                    }

                }
            }
//            System.out.println(location);
        }
        return null;
    }

    private String getFullText(ParseTree tree) {
        ParserRuleContext context = (ParserRuleContext) tree;
        if (context.children == null) {
            return "";
        }
        Token startToken = context.start;
        Token stopToken = context.stop;
        Interval interval = new Interval(startToken.getStartIndex(), stopToken.getStopIndex());
        return context.start.getInputStream().getText(interval);
    }

    private int getColumn(String text, String varName) {
        return text.indexOf(varName) + 4;
    }

    private boolean defineVar(String varName, Token token, String text) {
        Symbol symbol = currentScope.resolve(varName, true);
        if (currentScope == globalScope) {
            if (symbol != null) {
                error = true;
                System.err.println("Error type 3 at Line " + token.getLine() + ": Redefined variable: " + varName);
                return true;
            }
        }
        if (symbol != null && !symbol.getType().toString().equals("func")) {
            error = true;
            System.err.println("Error type 3 at Line " + token.getLine() + ": Redefined variable: " + varName);
            return true;
        } else {
            Type type;
            if (text.contains("]")) {
                int dimension = getDimension(text);
                type = new ArrayType(dimension);
            } else {
                type = new BasicType("int");
            }
            Symbol var = new VariableSymbol(varName, type);
            currentScope.define(var);
            return false;
        }
    }

    private int getDimension(String text){
        int res = 0;
        for(int i=0;i<text.length();i++){
            if(text.charAt(i)=='['){
                res++;
            }
        }
        return res;
    }

    @Override
    public Type visitLVal(SysYParser.LValContext ctx) {
        String varName = ctx.IDENT().getText();
        Symbol symbol = currentScope.resolve(varName, false);
        if (symbol == null) {
            Token token = ctx.getStart();
            error = true;
            System.err.println("Error type 1 at Line " + token.getLine() + ": Undefined variable: " + varName);
            return new ErrorType();
        }

        if (symbol.getType().toString().equals("func")) {
            if (ctx.getText().contains("]")) {
                error = true;
                System.err.println("Error type 9 at Line " + ctx.getStart().getLine() + ": Not an array: " + varName);
                return new ErrorType();
            }
            return symbol.getType();
        }
        Type type = symbol.getType();
        if (type.toString().equals("int")) {
            if (ctx.getText().contains("]")) {
                error = true;
                System.err.println("Error type 9 at Line " + ctx.getStart().getLine() + ": Not an array: " + varName);
                return new ErrorType();
            }
            return new BasicType("int");
        } else {
            int init_d = Integer.parseInt(type.toString().split("array")[1]);
            int curr_d = init_d - ctx.L_BRACKT().size();
            if (curr_d == 0) return new BasicType("int");
            if (curr_d < 0) {
                error = true;
                System.err.println("Error type 9 at Line " + ctx.getStart().getLine() + ": Not an array: " + varName);
                return new ErrorType();
            } else return new ArrayType(curr_d);
        }
    }

    @Override
    public Type visitExp_func(SysYParser.Exp_funcContext ctx) {
        String varName = ctx.IDENT().getText();
        Symbol symbol = currentScope.resolve(varName, false);
        // 检查变量未定义
        if (symbol == null) {
            Token token = ctx.getStart();
            error = true;
            System.err.println("Error type 2 at Line " + token.getLine() + ": Undefined function: " + varName);
            return new ErrorType();
        }
        if (!symbol.getType().toString().equals("func")) {
            Token token = ctx.getStart();
            error = true;
            System.err.println("Error type 10 at Line " + token.getLine() + ": Not a function: " + varName);
            return new ErrorType();
        }
        // 检查函数变量不匹配
        FuncType funcType = (FuncType) symbol.getType();
        // 函数形参为空
        if (funcType.paramsType.size() == 0 && ctx.funcRParams() != null || funcType.paramsType.size() != 0 && ctx.funcRParams() == null) {
            Token token = ctx.getStart();
            error = true;
            System.err.println("Error type 8 at Line " + token.getLine() + ": Function is not applicable for arguments");
            return new ErrorType();
        }
        //函数形参不为空
        List<Type> fParams = funcType.paramsType;
        int size = ctx.funcRParams() == null ? 0 : ctx.funcRParams().param().size();
        List<Type> rTypes = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Type rType = visit(ctx.funcRParams().param(i).exp());
            rTypes.add(rType);
        }
        if (size != fParams.size()) {
            Token token = ctx.getStart();
            error = true;
            System.err.println("Error type 8 at Line " + token.getLine() + ": Function is not applicable for arguments");
            return new ErrorType();
        }
        for (int i = 0; i < size; i++) {
            Type rType = rTypes.get(i);

            if (!fParams.get(i).toString().equals(rType.toString())) {
                error = true;
                System.err.println("Error type 8 at Line " + ctx.getStart().getLine() + ": Function is not applicable for arguments");
                return new ErrorType();

            }
        }

        return ((FuncType) symbol.getType()).returnType;
    }

    @Override
    public Type visitExp_number(SysYParser.Exp_numberContext ctx) {
        return new BasicType("int");
    }

    @Override
    public Type visitExp_paren(SysYParser.Exp_parenContext ctx) {
        return visit(ctx.exp());
    }

    @Override
    public Type visitExp_lVal(SysYParser.Exp_lValContext ctx) {
        return visitLVal(ctx.lVal());
    }

    @Override
    public Type visitExp_unary(SysYParser.Exp_unaryContext ctx) {
        Type type = visit(ctx.exp());
        if (type.toString().equals("error") || type.toString().equals("int")) {
            return type;
        } else {
            error = true;
            System.err.println("Error type 6 at Line " + ctx.getStart().getLine() + ": type.Type mismatched for operands.");
            return new ErrorType();
        }
    }

    @Override
    public Type visitExp_2(SysYParser.Exp_2Context ctx) {
        Type type1 = visit(ctx.exp(0));
        Type type2 = visit(ctx.exp(1));
        if (type1.toString().equals("error") || type2.toString().equals("error")) {
            return new ErrorType();
        }
        if (type1.toString().equals("int") && type2.toString().equals("int")) {
            return type1;
        } else {
            error = true;
            System.err.println("Error type 6 at Line " + ctx.getStart().getLine() + ": type.Type mismatched for operands.");
            return new ErrorType();
        }
    }

    @Override
    public Type visitExp_1(SysYParser.Exp_1Context ctx) {
        Type type1 = visit(ctx.exp(0));
        Type type2 = visit(ctx.exp(1));
        if (type1.toString().equals("error") || type2.toString().equals("error")) {
            return new ErrorType();
        }
        if (type1.toString().equals("int") && type2.toString().equals("int")) {
            return type1;
        } else {
            error = true;
            System.err.println("Error type 6 at Line " + ctx.getStart().getLine() + ": type.Type mismatched for operands.");
            return new ErrorType();
        }
    }


    @Override
    public Type visitStmt_assign(SysYParser.Stmt_assignContext ctx) {
        Type type1 = visit(ctx.lVal());
        Type type2 = visit(ctx.exp());
        if (type1.toString().equals("error")) {
            return null;
        }
        if (type1.toString().equals("func")) {
            error = true;
            System.err.println("Error type 11 at Line " + ctx.getStart().getLine() + ": The left-hand side of an assignment must be a variable.");
            return null;
        }
        if (type2.toString().equals("error")) {
            return null;
        }
        if (!type2.toString().equals(type1.toString())) {
            error = true;
            System.err.println("Error type 5 at Line " + ctx.getStart().getLine() + ": type.Type mismatched for assignment.");
            return null;
        }
        return null;
    }

    @Override
    public Type visitStmt_return(SysYParser.Stmt_returnContext ctx) {
        BaseScope scope = (BaseScope) currentScope;
        while (scope != null && !(scope instanceof FunctionSymbol))
            scope = (BaseScope) scope.getEnclosingScope();
        if (scope == null) return null;
        Type returnType = ((FunctionSymbol) scope).getType().returnType;
        if (ctx.exp() == null) {
            if (returnType.toString().equals("int")) {
                error = true;
                System.err.println("Error type 7 at Line " + ctx.getStart().getLine() + ": type.Type mismatched for return.");
                return new ErrorType();
            }
            return null;
        } else {
            Type type = visit(ctx.exp());
            if (type.toString().equals("error")) {
                return null;
            } else if (type.toString().equals("int")) {
                if (returnType.toString().equals("int")) {
                    return null;
                }
                error = true;
                System.err.println("Error type 7 at Line " + ctx.getStart().getLine() + ": type.Type mismatched for return.");
                return new ErrorType();
            } else if (type.toString().equals("func")) {
                FuncType funcType = (FuncType) type;
                if (funcType.returnType.toString().equals("void") && returnType.toString().equals("void")) {
                    return null;
                }
                error = true;
                System.err.println("Error type 7 at Line " + ctx.getStart().getLine() + ": type.Type mismatched for return.");
                return new ErrorType();
            } else {
                error = true;
                System.err.println("Error type 7 at Line " + ctx.getStart().getLine() + ": type.Type mismatched for return.");
                return new ErrorType();
            }
        }
    }

    @Override
    public Type visitCond_exp(SysYParser.Cond_expContext ctx) {
        return visit(ctx.exp());
    }

    private Type visitCond(SysYParser.CondContext cond1, SysYParser.CondContext cond2, int line) {
        Type type1 = visit(cond1);
        if (type1 == null || type1.toString().equals("error")) {
            return new ErrorType();
        }
        Type type2 = visit(cond2);
        if (type2 == null || type2.toString().equals("error")) {
            return new ErrorType();
        }
        if (!(type1.toString().equals("int") && type2.toString().equals("int"))) {
            error = true;
            System.err.println("Error type 6 at Line " + line + ": type.Type mismatched for operands.");
            return new ErrorType();
        }
        return type1;
    }

    @Override
    public Type visitCond_4(SysYParser.Cond_4Context ctx) {
        return visitCond(ctx.cond(0), ctx.cond(1), ctx.getStart().getLine());
    }


    @Override
    public Type visitCond_3(SysYParser.Cond_3Context ctx) {
        return visitCond(ctx.cond(0), ctx.cond(1), ctx.getStart().getLine());
    }

    @Override
    public Type visitCond_2(SysYParser.Cond_2Context ctx) {
        return visitCond(ctx.cond(0), ctx.cond(1), ctx.getStart().getLine());
    }

    @Override
    public Type visitCond_1(SysYParser.Cond_1Context ctx) {
        return visitCond(ctx.cond(0), ctx.cond(1), ctx.getStart().getLine());
    }
}
