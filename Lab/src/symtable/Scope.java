package symtable;

import java.util.Map;

public interface Scope {
    public String getName();                // 获取名称

    public void setName(String name);       // 设置名称

    public Scope getEnclosingScope();       // 获取外部作用域

    public Map<String, Symbol> getSymbols();// 获取所有符号

    public void define(Symbol symbol);      // 在作用域中定义符号

    public Symbol resolve(String name);     // 根据名称查找
}