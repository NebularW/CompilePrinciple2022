package symbol;

import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.LLVMTypeRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

public class FuncSymbol extends BaseScope implements Symbol{
    String name;

    LLVMValueRef valueRef;

    LLVMTypeRef funcType;

    LLVMTypeRef retType;

    int argumentSize;

    Scope enclosingScope;

    public FuncSymbol(String name, Scope enclosingScope, LLVMValueRef valueRef, LLVMTypeRef funcType, LLVMTypeRef retType, int argumentSize) {
        super(name, enclosingScope);
        this.name = name;
        this.enclosingScope = enclosingScope;
        this.valueRef = valueRef;
        this.funcType = funcType;
        this.retType = retType;
        this.argumentSize = argumentSize;
    }

    public String getName() {
        return name;
    }

    @Override
    public LLVMValueRef getVal() {
        return valueRef;
    }

    public LLVMTypeRef getType(){
        return funcType;
    }

    public int getArgumentSize(){
        return argumentSize;
    }

    public LLVMTypeRef getRetType(){return retType;}
}
