package symtable;

import antlr.SysYParser;
import antlr.SysYParserBaseListener;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

public class SymbolTableListener extends SysYParserBaseListener {
    private final SymbolTableTreeGraph graph = new SymbolTableTreeGraph();

    private GlobalScope globalScope =null;
    private Scope currentScope = null;
    private int localScopeCounter = 0;

    // Adding Code Below

    public SymbolTableTreeGraph getGraph() {
        return graph;
    }

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
        FunctionSymbol funcSymbol = new FunctionSymbol(funcName, currentScope);
        currentScope.define(funcSymbol);
        currentScope = funcSymbol;
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
    public void exitConstDecl(SysYParser.ConstDeclContext ctx) {
        String typeName = ctx.bType().getText();
        Type type = (Type) globalScope.resolve(typeName);
        for(SysYParser.ConstDefContext constDefContext: ctx.constDef()){
            String varName = constDefContext.IDENT().getText();
            VariableSymbol var = new VariableSymbol(varName, type);
            currentScope.define(var);
        }

    }

    @Override
    public void exitVarDecl(SysYParser.VarDeclContext ctx) {
        String typeName = ctx.bType().getText();
        Type type = (Type) globalScope.resolve(typeName);
        for(SysYParser.VarDefContext varDefContext: ctx.varDef()){
            String varName = varDefContext.IDENT().getText();
            VariableSymbol var = new VariableSymbol(varName, type);
            currentScope.define(var);
        }
    }

    @Override
    public void exitFuncFParam(SysYParser.FuncFParamContext ctx) {
        String typeName = ctx.bType().getText();
        Type type = (Type) globalScope.resolve(typeName);
        String varName = ctx.IDENT().getText();
        VariableSymbol var = new VariableSymbol(varName, type);
        currentScope.define(var);
    }


    // 以下是解析符号


    @Override
    public void visitTerminal(TerminalNode node) {
        Token token = node.getSymbol();
        if(token.getType() == 33){
            String varName = token.getText();
            currentScope.resolve(varName);
        }
    }
}