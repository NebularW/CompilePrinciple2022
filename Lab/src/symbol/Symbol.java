package symbol;

import org.bytedeco.llvm.LLVM.LLVMTypeRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;


public interface Symbol {
    public String getName();

    public LLVMValueRef getVal();

    public LLVMTypeRef getType();
}
