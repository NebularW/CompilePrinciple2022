package symtable;

import java.util.List;

public class FuncType implements Type {
    public String name;
    public Type returnType;
    public List<Type> paramsType;

    public FuncType(Type returnType, List<Type> paramsType) {
        this.name = "func";
        this.returnType = returnType;
        this.paramsType = paramsType;
    }

    public String toString(){
        return name;
    }
}
