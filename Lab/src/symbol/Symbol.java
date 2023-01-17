package symbol;

import org.bytedeco.llvm.LLVM.LLVMValueRef;


public interface Symbol {
    public String getName();

    public LLVMValueRef getVal();

    public void setVal(LLVMValueRef valueRef);
}
