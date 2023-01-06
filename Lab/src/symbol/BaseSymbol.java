package symbol;


import org.bytedeco.llvm.LLVM.LLVMValueRef;

public class BaseSymbol implements Symbol {
    final String name;
    LLVMValueRef valueRef;


    public BaseSymbol(String name, LLVMValueRef valueRef) {
        this.name = name;
        this.valueRef = valueRef;
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
    public void setVal(LLVMValueRef valueRef) {
        this.valueRef = valueRef;
    }
}
