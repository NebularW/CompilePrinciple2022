import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException {
        //需要输入文件路径
        if (args.length < 1) {
            System.err.println("input path is required");
        }
        //获取输入字符串
        String source = args[0];
        CharStream input = CharStreams.fromFileName(source);
        //获取词法分析器和语法分析器
        SysYLexer sysYLexer = new SysYLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(sysYLexer);
        SysYParser sysYParser = new SysYParser(tokens);
        //使用自行实现的ErrorListener
        sysYParser.removeErrorListeners();
        MyErrorListener myErrorListener = new MyErrorListener();
        sysYParser.addErrorListener(myErrorListener);
        //使用visitor
        ParseTree tree = sysYParser.program();
        MyVisitor visitor = new MyVisitor(sysYParser.getRuleNames(),sysYLexer.getVocabulary());
        if(!myErrorListener.getErrState()) visitor.visit(tree);
    }

}