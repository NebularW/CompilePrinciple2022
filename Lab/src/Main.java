import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.pattern.TokenTagToken;

import java.io.*;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("input path is required");
        }
        String source = args[0];
        CharStream input = CharStreams.fromFileName(source);
        SysYLexer sysYLexer = new SysYLexer(input);
        //使用自行实现的ErrorListener
        sysYLexer.removeErrorListeners();
        MyErrorListener myErrorListener = new MyErrorListener();
        sysYLexer.addErrorListener(myErrorListener);
        //如果没有错误，输出token，否则停止输出
        List<? extends Token> tokens = sysYLexer.getAllTokens();
        Vocabulary vocabulary = sysYLexer.getVocabulary();
       if(!myErrorListener.getErrState()){
           for (Token token : tokens) {
               String type = vocabulary.getSymbolicName(token.getType());
               String text = token.getText();
               int line = token.getLine();
               if (type.equals("INTEGR_CONST")) {
                   if (text.startsWith("0x") || text.startsWith("0X")) {
                       String original = text.substring(2);
                       text = String.valueOf(Integer.parseInt(original, 16));
                   } else if (text.startsWith("0") && text.length() > 1) {
                       String original = text.substring(1);
                       text = String.valueOf(Integer.parseInt(original, 8));
                   }
               }
               System.err.println(type + " " + text + " at Line " + line + ".");

           }
       }
    }

}