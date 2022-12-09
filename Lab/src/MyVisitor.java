import antlr.SysYParserBaseVisitor;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.Objects;

public class MyVisitor extends SysYParserBaseVisitor<Void> {
    private final String[] ruleNames;

    private final Vocabulary vocabulary;

    private final String[] color = {null,"orange","orange","orange","orange","orange","orange","orange","orange","orange",
            "blue","blue","blue","blue","blue","blue","blue","blue","blue","blue","blue","blue","blue","blue","blue",
            null,null,null,null,null,null,null,null,"red","green",null,null,null};

    public MyVisitor(String[] ruleNames, Vocabulary vocabulary){
        this.ruleNames = ruleNames;
        this.vocabulary = vocabulary;
    }

    @Override
    public Void visitChildren(RuleNode node) {
        String ruleName = ruleNames[node.getRuleContext().getRuleIndex()];
        char capital = (char) (ruleName.charAt(0) + 'A' - 'a');
        ruleName = capital + ruleName.substring(1);
        int depth = node.getRuleContext().depth();
        for(int i = 0; i < depth - 1; i++){
            System.err.print("  ");
        }
        System.err.println(ruleName);
        return super.visitChildren(node);
    }

    @Override
    public Void visitTerminal(TerminalNode node) {
        Token token = node.getSymbol();
        int type = token.getType();
        String text = token.getText();
        if(type>0 && !Objects.equals(color[type], null)){
            RuleNode parent = (RuleNode) node.getParent();
            int depth = parent.getRuleContext().depth() + 1;
            for(int i = 0; i < depth - 1; i++){
                System.err.print("  ");
            }
            if (type == 34) {
                if (text.startsWith("0x") || text.startsWith("0X")) {
                    String original = text.substring(2);
                    text = String.valueOf(Integer.parseInt(original, 16));
                } else if (text.startsWith("0") && text.length() > 1) {
                    String original = text.substring(1);
                    text = String.valueOf(Integer.parseInt(original, 8));
                }
            }
            System.err.println(text + " " + vocabulary.getSymbolicName(type) + "[" + color[type] + "]");
        }
        return super.visitTerminal(node);
    }
}
