import antlr.SysYLexer;
import antlr.SysYParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import symtable.SymbolTableListener;

import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException {
        //需要输入文件路径
        if (args.length < 1) {
            System.err.println("input path is required");
        }
        //获取输入参数
        String source = args[0];
        int lineNo = Integer.parseInt(args[1]);
        int column = Integer.parseInt(args[2]);
        String name = args[3];
        //获取输入字符串
        CharStream input = CharStreams.fromFileName(source);
        //获取词法分析器和语法分析器
        SysYLexer sysYLexer = new SysYLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(sysYLexer);
        SysYParser sysYParser = new SysYParser(tokens);
        //使用visitor
        ParseTree tree = sysYParser.program();
        MyVisitor visitor = new MyVisitor(sysYParser.getRuleNames(), sysYLexer.getVocabulary());
        visitor.visit(tree);


        //使用listener
//        ParseTree tree = sysYParser.program();
        ParseTreeWalker treeWalker = new ParseTreeWalker();
        SymbolTableListener symbolTableListener = new SymbolTableListener();
        treeWalker.walk(symbolTableListener, tree);


    }

}