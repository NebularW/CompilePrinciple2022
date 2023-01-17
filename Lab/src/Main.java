import antlr.SysYLexer;
import antlr.SysYParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import symtable.Symbol;
import symtable.SymbolTableVisitor;

import java.io.*;
import java.util.List;

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
        MyVisitor myVisitor= new MyVisitor(sysYParser.getRuleNames(), sysYLexer.getVocabulary());
        SymbolTableVisitor visitor = new SymbolTableVisitor();
        visitor.visit(tree);
        if(!visitor.error){
            myVisitor.visit(tree);
        }

        for(Object obj: myVisitor.msgToPrint){
            if(obj instanceof Symbol){
                if(((Symbol) obj).isUsed(lineNo,column)){
                    System.err.print(name);
                }else{
                    System.err.print(((Symbol) obj).getName());
                }
            }else{
                System.err.print(obj);
            }
        }


    }
}