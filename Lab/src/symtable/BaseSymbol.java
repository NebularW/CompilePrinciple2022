package symtable;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BaseSymbol implements Symbol {
    final String name;
    Type type;
    Set<String> use;

    public BaseSymbol(String name, Type type) {
        this.name = name;
        this.type = type;
        this.use = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    @Override
    public void setType(Type type) {
        this.type = type;
    }

    public String toString() {
        return "name: " + name + " type: " + type;
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
}
