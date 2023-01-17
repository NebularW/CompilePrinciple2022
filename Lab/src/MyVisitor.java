import antlr.SysYParser;
import antlr.SysYParserBaseVisitor;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.*;

import static org.bytedeco.llvm.global.LLVM.*;

public class MyVisitor extends SysYParserBaseVisitor<LLVMValueRef> {
    //输出路径
    String dest;
    //创建module
    LLVMModuleRef module = LLVMModuleCreateWithName("module");
    //初始化IRBuilder，后续将使用这个builder去生成LLVM IR
    LLVMBuilderRef builder = LLVMCreateBuilder();
    //考虑到我们的语言中仅存在int一个基本类型，可以通过下面的语句为LLVM的int型重命名方便以后使用
    LLVMTypeRef i32Type = LLVMInt32Type();
    //用于输出到文件
    public static final BytePointer error = new BytePointer();
    // 常量
    LLVMValueRef negative1 = LLVMConstInt(i32Type, -1, /* signExtend */ 0);
    // 常量-1
    LLVMValueRef positive1 = LLVMConstInt(i32Type, 1, /* signExtend */ 0);
    LLVMValueRef zero = LLVMConstInt(i32Type, 0, /* signExtend */ 0);

    public MyVisitor(String dest) {
        this.dest = dest;
        //初始化LLVM
        LLVMInitializeCore(LLVMGetGlobalPassRegistry());
        LLVMLinkInMCJIT();
        LLVMInitializeNativeAsmPrinter();
        LLVMInitializeNativeAsmParser();
        LLVMInitializeNativeTarget();
    }

    @Override
    public LLVMValueRef visitProgram(SysYParser.ProgramContext ctx) {
        LLVMValueRef valueRef = super.visitProgram(ctx);
        // 控制台输出
//        LLVMDumpModule(module);
        // 文件输出
        if (LLVMPrintModuleToFile(module, dest, error) != 0) {    // module是你自定义的LLVMModuleRef对象
            LLVMDisposeMessage(error);
        }
        return valueRef;
    }

    @Override
    public LLVMValueRef visitFuncDef(SysYParser.FuncDefContext ctx) {
        //生成返回值类型
        LLVMTypeRef returnType = i32Type;

        //生成函数参数类型
        PointerPointer<Pointer> argumentTypes = new PointerPointer<>(0);

        //生成函数类型
        LLVMTypeRef ft = LLVMFunctionType(returnType, argumentTypes, /* argumentCount */ 0, /* isVariadic */ 0);

        //生成函数，即向之前创建的module中添加函数
        LLVMValueRef function = LLVMAddFunction(module, /*functionName:String*/ctx.IDENT().toString(), ft);

        //在函数中添加基本块
        LLVMBasicBlockRef block = LLVMAppendBasicBlock(function, /*blockName:String*/"mainEntry");

        //选择要在哪个基本块后追加指令
        LLVMPositionBuilderAtEnd(builder, block);//后续生成的指令将追加在block1的后面

        return super.visitFuncDef(ctx);
    }

    @Override
    public LLVMValueRef visitStmt_return(SysYParser.Stmt_returnContext ctx) {
        LLVMValueRef result = visit(ctx.exp());
        LLVMBuildRet(builder, result);

        return super.visitStmt_return(ctx);

    }

    @Override
    public LLVMValueRef visitExp_2(SysYParser.Exp_2Context ctx) {
//        LLVMValueRef ret = super.visitExp_2(ctx);
        LLVMValueRef value1 = visit(ctx.exp(0));
        LLVMValueRef value2 = visit(ctx.exp(1));
        if (ctx.MUL() != null) {
            return LLVMBuildMul(builder, value1, value2, "");
        } else if (ctx.DIV() != null) {
            return LLVMBuildSDiv(builder, value1, value2, "");
        } else {
            LLVMValueRef q = LLVMBuildSDiv(builder, value1, value2, "");
            LLVMValueRef accumulate = LLVMBuildMul(builder, q, value2, "");
            return LLVMBuildSub(builder, value1, accumulate, "");
        }
    }

    @Override
    public LLVMValueRef visitExp_1(SysYParser.Exp_1Context ctx) {
//        LLVMValueRef ret = super.visitExp_1(ctx);
        LLVMValueRef value1 = visit(ctx.exp(0));
        LLVMValueRef value2 = visit(ctx.exp(1));
        if (ctx.PLUS() != null) {
            return LLVMBuildAdd(builder, value1, value2, "");
        } else {
            return LLVMBuildSub(builder, value1, value2, "");
        }
    }

    @Override
    public LLVMValueRef visitExp_number(SysYParser.Exp_numberContext ctx) {
        String text = ctx.getText();
        if (text.startsWith("0x") || text.startsWith("0X")) {
            String original = text.substring(2);
            text = String.valueOf(Integer.parseInt(original, 16));
        } else if (text.startsWith("0") && text.length() > 1) {
            String original = text.substring(1);
            text = String.valueOf(Integer.parseInt(original, 8));
        }
        int num = Integer.parseInt(text);
        return LLVMConstInt(i32Type, num, 0);

    }

    @Override
    public LLVMValueRef visitExp_unary(SysYParser.Exp_unaryContext ctx) {
//        LLVMValueRef ret = super.visitExp_unary(ctx);
        LLVMValueRef original = visit(ctx.exp());
        String op = ctx.unaryOp().getText();
        switch (op) {
            case "+":
                return original;
            case "-":
                return LLVMBuildMul(builder, original, negative1, "-1");
            case "!":
                if (original.equals(zero)) return positive1;
                else return zero;
            default:
//                return ret;
                return null;
        }
    }
}
