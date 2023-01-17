package symbol;


import org.bytedeco.llvm.LLVM.LLVMTypeRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

public class BaseSymbol implements Symbol {
    final String name;
    LLVMValueRef valueRef;

    LLVMTypeRef typeRef;


    public BaseSymbol(String name, LLVMValueRef valueRef, LLVMTypeRef typeRef) {
        this.name = name;
        this.valueRef = valueRef;
        this.typeRef = typeRef;
    }


    @Override
    public String getName() {
        return name;
    }

    @Override
    public LLVMValueRef getVal() {
        return valueRef;
    }

    @Override
    public LLVMTypeRef getType() {
        return typeRef;
    }
}
