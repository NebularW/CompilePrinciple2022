package symtable;

public class BasicType implements Type{
    String name;

    public BasicType(String name){
        this.name = name;
    }

    public String toString(){
        return name;
    }
}
