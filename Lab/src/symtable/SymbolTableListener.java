package symtable;

import antlr.SysYParser;
import antlr.SysYParserBaseListener;
import org.antlr.v4.runtime.Token;


import java.util.ArrayList;
import java.util.List;

public class SymbolTableListener extends SysYParserBaseListener {
    private GlobalScope globalScope = null;
    private Scope currentScope = null;
    private int localScopeCounter = 0;
    boolean error = false;

    // 以下是更改作用域

    @Override
    public void enterProgram(SysYParser.ProgramContext ctx) {
        globalScope = new GlobalScope(null);
        currentScope = globalScope;
    }

    @Override
    public void enterFuncDef(SysYParser.FuncDefContext ctx) {
        String typeName = ctx.funcType().getText();
        globalScope.resolve(typeName);
        String funcName = ctx.IDENT().getText();
        if (currentScope.resolve(funcName) != null) {
            error = true;
            System.err.println("Error type 4 at Line " + ctx.getStart().getLine() + ": Redefined function: " + funcName);
            ctx.removeLastChild();
        } else {
            Type returnType = new BasicTypeSymbol(ctx.funcType().getText());
            List<Type> paramTypes = new ArrayList<>();
            if (ctx.funcFParams() != null) {
                for (SysYParser.FuncFParamContext paramContext : ctx.funcFParams().funcFParam()) {
                    String text = paramContext.getText();
                    Type type;
                    if (text.contains("]")) {
                        int dimension = text.split("]").length;
                        type = new ArrayType(dimension);
                    } else {
                        type = (Type) globalScope.resolve("int");
                    }
                    paramTypes.add(type);
                }
            }
            FuncType type = new FuncType(returnType, paramTypes);
            FunctionSymbol funcSymbol = new FunctionSymbol(funcName, currentScope, type);
            currentScope.define(funcSymbol);
            currentScope = funcSymbol;
        }

    }

    @Override
    public void enterBlock(SysYParser.BlockContext ctx) {
        LocalScope localScope = new LocalScope(currentScope);
        String localScopeName = localScope.getName() + localScopeCounter;
        localScope.setName(localScopeName);
        localScopeCounter++;
        currentScope = localScope;
    }

    @Override
    public void exitProgram(SysYParser.ProgramContext ctx) {
        currentScope = currentScope.getEnclosingScope();
    }

    @Override
    public void exitFuncDef(SysYParser.FuncDefContext ctx) {
        currentScope = currentScope.getEnclosingScope();
    }

    @Override
    public void exitBlock(SysYParser.BlockContext ctx) {
        currentScope = currentScope.getEnclosingScope();
    }

    // 以下是更改符号

    @Override
    public void exitConstDef(SysYParser.ConstDefContext ctx) {
        defineVar(ctx.IDENT().getText(), ctx.getStart(), ctx.getText());
    }

    @Override
    public void exitVarDef(SysYParser.VarDefContext ctx) {
        defineVar(ctx.IDENT().getText(), ctx.getStart(), ctx.getText());
    }

    private void defineVar(String varName, Token token, String text) {
        Symbol symbol = currentScope.resolve(varName);
        if (symbol != null) {
            error = true;
            System.err.println("Error type 3 at Line " + token.getLine() + ": Redefined variable: " + varName);
        } else {
            Type type;
            if (text.contains("]")) {
                int dimension = text.split("]").length;
                type = new ArrayType(dimension);
            } else {
                type = (Type) globalScope.resolve("int");
            }
            Symbol var = new VariableSymbol(varName, type);
            currentScope.define(var);
        }
    }

    @Override
    public void enterFuncFParams(SysYParser.FuncFParamsContext ctx) {
        List<Type> fParamList = new ArrayList<>();
        for(SysYParser.FuncFParamContext param: ctx.funcFParam()){
            String varName = param.IDENT().getText();
            // 变量重名
            if(currentScope.resolve(varName) != null){
                Token token = ctx.getStart();
                error = true;
                System.err.println("Error type 3 at Line " + token.getLine() + ": Redefined variable: " + varName);
            }else{
                String text = ctx.getText();
                Type type;
                if (text.contains("]")) {
                    int dimension = text.split("]").length;
                    type = new ArrayType(dimension);
                } else {
                    type = new BasicTypeSymbol("int");
                }
                VariableSymbol var = new VariableSymbol(varName, type);
                fParamList.add(type);
                currentScope.define(var);
            }
        }
        if(fParamList.size() == ctx.funcFParam().size()) return;
        try{
            FunctionSymbol functionSymbol = (FunctionSymbol)currentScope;
            FuncType type = functionSymbol.getType();
            type.paramsType = fParamList;
        }catch (Exception e){
            System.out.println("Class Cast Exception");
        }

    }


    @Override
    public void exitLVal(SysYParser.LValContext ctx) {
        String varName = ctx.IDENT().getText();
        Symbol symbol = currentScope.resolve(varName);
        if (symbol == null) {
            Token token = ctx.getStart();
            error = true;
            System.err.println("Error type 1 at Line " + token.getLine() + ": Undefined variable: " + varName);
        }
    }

    @Override
    public void exitExp_func(SysYParser.Exp_funcContext ctx) {
        String varName = ctx.IDENT().getText();
        Symbol symbol = currentScope.resolve(varName);
        // 检查变量未定义
        if (symbol == null) {
            Token token = ctx.getStart();
            error = true;
            System.err.println("Error type 2 at Line " + token.getLine() + ": Undefined function: " + varName);
            return;
        }
        // 检查函数变量不匹配
        FuncType funcType = (FuncType) symbol.getType();
        // 函数形参为空
        if (funcType == null || funcType.paramsType == null || funcType.paramsType.size() == 0) {
            if (ctx.funcRParams() != null) {
                Token token = ctx.getStart();
                error = true;
                System.err.println("Error type 8 at Line " + token.getLine() + ": wrong function params: " + varName);
            }
        } else {
            //函数形参不为空
            List<Type> fParams = funcType.paramsType;
            int size = ctx.funcRParams() == null ? 0 : ctx.funcRParams().param().size();
            if (size != fParams.size()) {
                Token token = ctx.getStart();
                error = true;
                System.err.println("Error type 8 at Line " + token.getLine() + ": wrong function params: " + token.getText());
                return;
            }
            int i = 0;
            //TODO i+1  param match
//            for (SysYParser.ParamContext context : ctx.funcRParams().param()) {
//                Symbol param = currentScope.resolve(context.getText());
//                if (param == null || param.getType() == null || fParams.get(i) == null || !Objects.equals(param.getType().toString(), fParams.get(i).toString())) {
//                    Token token = ctx.getStart();
//                    error = true;
//                    System.err.println("Error type 8 at Line " + token.getLine() + ": wrong function params: " + token.getText());
//                    return;
//                }
//                i++;
//            }
        }
    }

    @Override
    public void exitExp_2(SysYParser.Exp_2Context ctx) {

    }

    @Override
    public void exitExp_1(SysYParser.Exp_1Context ctx) {
        super.exitExp_1(ctx);
    }
}