package symtable;

public class ArrayType implements Type{
    public String name;
    public int dimension = 0;

    public ArrayType(int d){
        this.name = "array"+d;
        this.dimension = d;
    }

    public String toString(){
        return name;
    }
}
