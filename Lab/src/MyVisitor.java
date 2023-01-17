import antlr.SysYParser;
import antlr.SysYParserBaseVisitor;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import symtable.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MyVisitor extends SysYParserBaseVisitor<Void> {
    private final String[] ruleNames;

    private final Vocabulary vocabulary;

    private final String[] color = {null,"orange","orange","orange","orange","orange","orange","orange","orange","orange",
            "blue","blue","blue","blue","blue","blue","blue","blue","blue","blue","blue","blue","blue","blue","blue",
            null,null,null,null,null,null,null,null,"red","green",null,null,null};

    private GlobalScope globalScope = null;
    private Scope currentScope = null;
    private int localScopeCounter = 0;

    public List<Object> msgToPrint = new ArrayList<>();

    public MyVisitor(String[] ruleNames, Vocabulary vocabulary){
        this.ruleNames = ruleNames;
        this.vocabulary = vocabulary;
    }

    @Override
    public Void visitChildren(RuleNode node) {
        StringBuffer sb = new StringBuffer();
        String ruleName = ruleNames[node.getRuleContext().getRuleIndex()];
        char capital = (char) (ruleName.charAt(0) + 'A' - 'a');
        ruleName = capital + ruleName.substring(1);
        int depth = node.getRuleContext().depth();
        for(int i = 0; i < depth - 1; i++){
            sb.append("  ");
        }
        sb.append(ruleName).append(System.lineSeparator());
        msgToPrint.add(sb.toString());
        return super.visitChildren(node);
    }

    @Override
    public Void visitTerminal(TerminalNode node) {
        Token token = node.getSymbol();
        int type = token.getType();
        String text = token.getText();
        StringBuffer sb = new StringBuffer();
        if(type>0 && !Objects.equals(color[type], null)){
            RuleNode parent = (RuleNode) node.getParent();
            int depth = parent.getRuleContext().depth() + 1;
            for(int i = 0; i < depth - 1; i++){
                sb.append("  ");
            }
            msgToPrint.add(sb.toString());
            if (type == 34) {
                if (text.startsWith("0x") || text.startsWith("0X")) {
                    String original = text.substring(2);
                    text = String.valueOf(Integer.parseInt(original, 16));
                } else if (text.startsWith("0") && text.length() > 1) {
                    String original = text.substring(1);
                    text = String.valueOf(Integer.parseInt(original, 8));
                }
            }
            if(type == 33){
                Symbol symbol = currentScope.resolve(text, false);
                symbol.addUse(token.getLine(),token.getCharPositionInLine());
                msgToPrint.add(symbol);
            }else{
                msgToPrint.add(text);
            }
            sb = new StringBuffer();
            sb.append(" ").append(vocabulary.getSymbolicName(type)).append("[").append(color[type]).append("]").append(System.lineSeparator());
            msgToPrint.add(sb);
        }
        return super.visitTerminal(node);
    }

    @Override
    public Void visitProgram(SysYParser.ProgramContext ctx) {
        globalScope = new GlobalScope(null);
        currentScope = globalScope;
        Void ret = super.visitProgram(ctx);
        currentScope = currentScope.getEnclosingScope();
        return ret;
    }

    @Override
    public Void visitBlock(SysYParser.BlockContext ctx) {
        Void ret;
        if (ctx.getParent().getRuleIndex() != 10) {
            LocalScope localScope = new LocalScope(currentScope);
            String localScopeName = localScope.getName() + localScopeCounter;
            localScope.setName(localScopeName);
            localScopeCounter++;
            currentScope = localScope;
            ret = super.visitBlock(ctx);
            currentScope = currentScope.getEnclosingScope();
        } else {
            ret = super.visitBlock(ctx);
        }
        return ret;
    }

    @Override
    public Void visitConstDef(SysYParser.ConstDefContext ctx) {
        String varName = ctx.IDENT().getText();
        Type type;
        if (ctx.L_BRACKT().size()!=0) {
            int dimension = ctx.L_BRACKT().size();
            type = new ArrayType(dimension);
        } else {
            type = new BasicTypeSymbol("int");
        }
        VariableSymbol var = new VariableSymbol(varName, type);
        currentScope.define(var);
        return super.visitConstDef(ctx);
    }

    @Override
    public Void visitVarDef(SysYParser.VarDefContext ctx) {
        String varName = ctx.IDENT().getText();
        Type type;
        if (ctx.L_BRACKT().size()!=0) {
            int dimension = ctx.L_BRACKT().size();
            type = new ArrayType(dimension);
        } else {
            type = new BasicTypeSymbol("int");
        }
        VariableSymbol var = new VariableSymbol(varName, type);
        currentScope.define(var);
        return super.visitVarDef(ctx);
    }

    @Override
    public Void visitFuncDef(SysYParser.FuncDefContext ctx) {
        String funcName = ctx.IDENT().getText();
        Type returnType = new BasicTypeSymbol(ctx.funcType().getText());
        List<Type> paramTypes = new ArrayList<>();
        FuncType type = new FuncType(returnType, paramTypes);
        FunctionSymbol funcSymbol = new FunctionSymbol(funcName, currentScope, type);
        currentScope.define(funcSymbol);
        currentScope = funcSymbol;
        Void ret = super.visitFuncDef(ctx);
        currentScope = currentScope.getEnclosingScope();
        return ret;
    }

    @Override
    public Void visitFuncFParam(SysYParser.FuncFParamContext ctx) {
        String varName = ctx.IDENT().getText();
        Type type;
        if (ctx.L_BRACKT().size()!=0) {
            int dimension = ctx.L_BRACKT().size();
            type = new ArrayType(dimension);
        } else {
            type = new BasicTypeSymbol("int");
        }
        VariableSymbol var = new VariableSymbol(varName, type);
        currentScope.define(var);
        return super.visitFuncFParam(ctx);
    }

    @Override
    public Void visitLVal(SysYParser.LValContext ctx) {

        return super.visitLVal(ctx);
    }
}
