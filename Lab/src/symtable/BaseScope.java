package symtable;

import java.util.LinkedHashMap;
import java.util.Map;

public class BaseScope implements Scope {
    private final Scope enclosingScope;                                 // 父作用域
    private final Map<String, Symbol> symbols = new LinkedHashMap<>();  // 符号表
    private String name;                                                // 函数名

    public BaseScope(String name, Scope enclosingScope) {
        this.name = name;
        this.enclosingScope = enclosingScope;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Scope getEnclosingScope() {
        return this.enclosingScope == null ? this : this.enclosingScope;
    }

    public Map<String, Symbol> getSymbols() {
        return this.symbols;
    }

    @Override
    public void define(Symbol symbol) {
        symbols.put(symbol.getName(), symbol);
    }

    @Override
    public Symbol resolve(String name) {
        Symbol symbol = symbols.get(name);
        // 当前作用域找到了该符号
        if (symbol != null) {
            return symbol;
        }
        // 递归去父作用域查找
        if (enclosingScope != null) {
            return enclosingScope.resolve(name);
        }
        //没有找到
        return null;
    }

    @Override
    public String toString() {
        return "name: " + name + " symbols: " + symbols.values();
    }
}
