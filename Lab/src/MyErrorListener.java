import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

public class MyErrorListener extends BaseErrorListener {
    public static final MyErrorListener INSTANCE = new MyErrorListener();

    private boolean err = false; //判断是否有错误

    public MyErrorListener() {
    }

    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        this.err = true;
        System.err.println("Error type B at Line " + line + ": " + msg);
    }

    public boolean getErrState(){
        return err;
    }
}
