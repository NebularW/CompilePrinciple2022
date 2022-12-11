package symtable;

public class FunctionSymbol extends BaseScope implements Symbol {
    private FuncType type;
    public FunctionSymbol(String name, Scope enclosingScope, FuncType type) {
        super(name, enclosingScope);
        this.type = type;
    }

    @Override
    public FuncType getType() {
        return type;
    }

    public void setType(FuncType type){
        this.type = type;
    }
}