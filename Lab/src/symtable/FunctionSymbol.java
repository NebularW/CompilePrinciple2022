package symtable;


import java.util.HashSet;
import java.util.Set;

public class FunctionSymbol extends BaseScope implements Symbol {
    private FuncType type;

    Set<String> use;

    public FunctionSymbol(String name, Scope enclosingScope, FuncType type) {
        super(name, enclosingScope);
        this.type = type;
        this.use = new HashSet<>();
    }

    @Override
    public FuncType getType() {
        return type;
    }

    @Override
    public void setType(Type type) {
        this.type = (FuncType) type;
    }

    @Override
    public void addUse(int line, int col) {
        String location = line + " " + col;
        use.add(location);
    }

    @Override
    public boolean isUsed(int line, int col) {
        String location = line + " " + col;
        return use.contains(location);
    }


    public void setType(FuncType type){
        this.type = type;
    }

}