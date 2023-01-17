package symtable;

import java.util.List;

public interface Symbol {
    public String getName();

    public Type getType();

    public void setType(Type type);

    public void addUse(int line, int col);

    public boolean isUsed(int line, int col);
}
