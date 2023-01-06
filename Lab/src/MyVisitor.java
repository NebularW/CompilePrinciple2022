import antlr.SysYParser;
import antlr.SysYParserBaseVisitor;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.*;
import symbol.*;


import java.util.List;

import static org.bytedeco.llvm.global.LLVM.*;

public class MyVisitor extends SysYParserBaseVisitor<LLVMValueRef> {
    private GlobalScope globalScope = null;
    private Scope currentScope = null;
    private int localScopeCounter = 0;
    //输出路径
    String dest;
    //创建module
    LLVMModuleRef module = LLVMModuleCreateWithName("module");
    //初始化IRBuilder，后续将使用这个builder去生成LLVM IR
    LLVMBuilderRef builder = LLVMCreateBuilder();
    //考虑到我们的语言中仅存在int一个基本类型，可以通过下面的语句为LLVM的int型重命名方便以后使用
    LLVMTypeRef i32Type = LLVMInt32Type();
    LLVMTypeRef voidType = LLVMVoidType();
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
        globalScope = new GlobalScope(null);
        currentScope = globalScope;
        LLVMValueRef valueRef = super.visitProgram(ctx);
        currentScope = currentScope.getEnclosingScope();
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
        LLVMTypeRef returnType = ctx.funcType().getText().equals("int") ? i32Type : voidType;
        //生成函数参数类型
        int argumentSize = ctx.funcFParams() == null ? 0 : ctx.funcFParams().funcFParam().size();
        PointerPointer<Pointer> argumentTypes = new PointerPointer<>(argumentSize);
        for (int i = 0; i < argumentSize; i++) {
            argumentTypes.put(i, i32Type);
        }
        //生成函数类型
        LLVMTypeRef ft = LLVMFunctionType(returnType, argumentTypes, /* argumentCount */ argumentSize, /* isVariadic */ 0);

        //生成函数，即向之前创建的module中添加函数
        LLVMValueRef function = LLVMAddFunction(module, /*functionName:String*/ctx.IDENT().toString(), ft);

        //在函数中添加基本块
        LLVMBasicBlockRef block = LLVMAppendBasicBlock(function, /*blockName:String*/ctx.IDENT().toString() + "Entry");

        //选择要在哪个基本块后追加指令
        LLVMPositionBuilderAtEnd(builder, block);//后续生成的指令将追加在block1的后面

        // 创建新的作用域
        // 函数变量加入符号表
        FuncSymbol funcSymbol = new FuncSymbol(ctx.IDENT().toString(), currentScope, function, ft, returnType, argumentSize);
        currentScope.define(funcSymbol);
        currentScope = funcSymbol;
        // 访问子节点
        LLVMValueRef valueRef = super.visitFuncDef(ctx);
        if(!haveRet(ctx.block().blockItem())){
            if(returnType.equals(voidType)) LLVMBuildRetVoid(builder);
            else LLVMBuildRet(builder,zero);
        }
        // 退出函数作用域
        currentScope = currentScope.getEnclosingScope();
        return valueRef;
    }

    private boolean haveRet(List<SysYParser.BlockItemContext> items){
        for(SysYParser.BlockItemContext itemContext: items){
            if(itemContext.stmt() instanceof SysYParser.Stmt_returnContext){
                return true;
            }
        }
        return false;
    }


    @Override
    public LLVMValueRef visitFuncFParams(SysYParser.FuncFParamsContext ctx) {
        int size = ctx.funcFParam().size();
        for (int i = 0; i < size; i++) {
            //int型变量
            //申请一块能存放int型的内存
            LLVMValueRef pointer = LLVMBuildAlloca(builder, i32Type, /*pointerName:String*/"pointer_" + ctx.funcFParam(i).IDENT().toString());
            //将数值存入该内存
            SysYParser.FuncDefContext funcDefContext = (SysYParser.FuncDefContext) ctx.parent;
            Symbol func = currentScope.resolve(funcDefContext.IDENT().toString(), false);
            LLVMValueRef valueRef = LLVMGetParam(func.getVal(), /* parameterIndex */i);
            LLVMBuildStore(builder, valueRef, pointer);
            // 变量存入符号表
            BaseSymbol varSymbol = new BaseSymbol(ctx.funcFParam(i).IDENT().toString(), pointer);
            currentScope.define(varSymbol);
        }
        return null;
    }

    @Override
    public LLVMValueRef visitBlock(SysYParser.BlockContext ctx) {
        if (ctx.getParent().getRuleIndex() != 10) {
            LocalScope localScope = new LocalScope(currentScope);
            String localScopeName = localScope.getName() + localScopeCounter;
            localScope.setName(localScopeName);
            localScopeCounter++;
            currentScope = localScope;
            LLVMValueRef valueRef = super.visitBlock(ctx);
            currentScope = currentScope.getEnclosingScope();
            return valueRef;
        } else {
            return super.visitBlock(ctx);
        }
    }

    @Override
    public LLVMValueRef visitConstDef(SysYParser.ConstDefContext ctx) {
        LLVMValueRef pointer;
        // 判断是int还是数组，翻译局部变量
        if (ctx.L_BRACKT().size() == 0) {
            //int型变量
            //申请一块能存放int型的内存
            pointer = LLVMBuildAlloca(builder, i32Type, /*pointerName:String*/"pointer_" + ctx.IDENT().toString());
            //将数值存入该内存
            if (ctx.constInitVal() != null) {
                LLVMValueRef initVal = visit(ctx.constInitVal());
                LLVMBuildStore(builder, initVal, pointer);
            }
        } else {
            //数组变量
            int size = Integer.parseInt(ctx.constExp().get(0).exp().getText());
            //创建可存放多个int的array类型
            LLVMTypeRef arrayType = LLVMArrayType(i32Type, size);
            //申请一个可存放该vector类型的内存
            pointer = LLVMBuildAlloca(builder, arrayType, "pointer_" + ctx.IDENT().toString());
            // 获得数组的初始值，若无则为0
            LLVMValueRef[] initVal = new LLVMValueRef[size];
            for (int i = 0; i < size; i++) initVal[i] = zero;
            int initSize = ctx.constInitVal().constInitVal().size();
            for (int i = 0; i < initSize; i++) {
                initVal[i] = visit(ctx.constInitVal().constInitVal(i));
            }
            //GEP
            LLVMValueRef[] arrayPointer = new LLVMValueRef[2];
            arrayPointer[0] = zero;
            arrayPointer[1] = zero;
            for (int i = 0; i < size; i++) {
                arrayPointer[1] = LLVMConstInt(i32Type, i, /* signExtend */ 0);
                PointerPointer<LLVMValueRef> valuePointer = new PointerPointer<>(arrayPointer);
                LLVMValueRef elementPtr = LLVMBuildGEP(builder, pointer, valuePointer, 2, "GEP_" + i);
                LLVMBuildStore(builder, initVal[i], elementPtr);
            }


        }
        BaseSymbol symbol = new BaseSymbol(ctx.IDENT().toString(), pointer);
        currentScope.define(symbol);
        return pointer;
    }

    @Override
    public LLVMValueRef visitVarDef(SysYParser.VarDefContext ctx) {
        LLVMValueRef pointer;
        // 判断是int还是数组，翻译局部变量
        if (ctx.L_BRACKT().size() == 0) {
            //int型变量
            //申请一块能存放int型的内存
            pointer = LLVMBuildAlloca(builder, i32Type, /*pointerName:String*/"pointer_" + ctx.IDENT().toString());
            //将数值存入该内存
            if (ctx.initVal() != null) {
                LLVMValueRef initVal = visit(ctx.initVal());
                LLVMBuildStore(builder, initVal, pointer);
            }
        } else {
            //数组变量
            int size = Integer.parseInt(ctx.constExp().get(0).exp().getText());
            //创建可存放多个int的vector类型
            LLVMTypeRef arrayType = LLVMArrayType(i32Type, size);
            //申请一个可存放该vector类型的内存
            pointer = LLVMBuildAlloca(builder, arrayType, "pointer_" + ctx.IDENT().toString());
            // 获得数组的初始值，若无则为0
            LLVMValueRef[] initVal = new LLVMValueRef[size];
            for (int i = 0; i < size; i++) initVal[i] = zero;
            int initSize = ctx.initVal().initVal().size();
            for (int i = 0; i < initSize; i++) {
                initVal[i] = visit(ctx.initVal().initVal(i));
            }
            //GEP
            LLVMValueRef[] arrayPointer = new LLVMValueRef[2];
            arrayPointer[0] = zero;
            arrayPointer[1] = zero;
            for (int i = 0; i < size; i++) {
                arrayPointer[1] = LLVMConstInt(i32Type, i, /* signExtend */ 0);
                PointerPointer<LLVMValueRef> valuePointer = new PointerPointer<>(arrayPointer);
                LLVMValueRef elementPtr = LLVMBuildGEP(builder, pointer, valuePointer, 2, "GEP_" + i);
                LLVMBuildStore(builder, initVal[i], elementPtr);
            }
        }
        BaseSymbol symbol = new BaseSymbol(ctx.IDENT().toString(), pointer);
        currentScope.define(symbol);
        return pointer;
    }

    @Override
    public LLVMValueRef visitStmt_return(SysYParser.Stmt_returnContext ctx) {
        if (ctx.exp() != null) {
            LLVMValueRef result = visit(ctx.exp());
            LLVMBuildRet(builder, result);
            return result;
        } else {
            return LLVMBuildRetVoid(builder);
        }

    }

    @Override
    public LLVMValueRef visitStmt_assign(SysYParser.Stmt_assignContext ctx) {
        LLVMValueRef lVal = visit(ctx.lVal());
        LLVMValueRef rVal = visit(ctx.exp());
        return LLVMBuildStore(builder, rVal, lVal);
    }

    @Override
    public LLVMValueRef visitExp_2(SysYParser.Exp_2Context ctx) {
        LLVMValueRef value1 = visit(ctx.exp(0));
        LLVMValueRef value2 = visit(ctx.exp(1));
        if (ctx.MUL() != null) {
            return LLVMBuildMul(builder, value1, value2, "mul_");
        } else if (ctx.DIV() != null) {
            return LLVMBuildSDiv(builder, value1, value2, "div_");
        } else {
            return LLVMBuildSRem(builder, value1, value2, "rem_");
        }
    }

    @Override
    public LLVMValueRef visitExp_1(SysYParser.Exp_1Context ctx) {
        LLVMValueRef value1 = visit(ctx.exp(0));
        LLVMValueRef value2 = visit(ctx.exp(1));
        if (ctx.PLUS() != null) {
            return LLVMBuildAdd(builder, value1, value2, "add_");
        } else {
            return LLVMBuildSub(builder, value1, value2, "sub_");
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
                return null;
        }
    }


    @Override
    public LLVMValueRef visitLVal(SysYParser.LValContext ctx) {
        Symbol var = currentScope.resolve(ctx.IDENT().getText(), false);
        LLVMValueRef varVal = var.getVal();
        if (ctx.L_BRACKT().size() == 0) {
            if (ctx.getParent() instanceof SysYParser.Stmt_assignContext) {
                return varVal;
            }
            return LLVMBuildLoad(builder, varVal, /*varName:String*/ctx.IDENT().getText());
        } else {
            LLVMValueRef[] arrayPointer = new LLVMValueRef[2];
            arrayPointer[0] = zero;
            arrayPointer[1] = visit(ctx.exp(0));
            PointerPointer<LLVMValueRef> valuePointer = new PointerPointer<>(arrayPointer);
            LLVMValueRef elementPtr = LLVMBuildGEP(builder, varVal, valuePointer, 2, ctx.IDENT().getText()+"["+ ctx.exp(0).getText()+"]");
            if (ctx.getParent() instanceof SysYParser.Stmt_assignContext) {
                return elementPtr;
            }
            return LLVMBuildLoad(builder, elementPtr, /*varName:String*/ctx.IDENT().getText() + 1);
        }
    }

    @Override
    public LLVMValueRef visitExp_func(SysYParser.Exp_funcContext ctx) {
        Symbol var = currentScope.resolve(ctx.IDENT().getText(), false);
        FuncSymbol funcSymbol = (FuncSymbol) var;
        PointerPointer<Pointer> arguments = new PointerPointer<>(funcSymbol.getArgumentSize());
        for (int i = 0; i < funcSymbol.getArgumentSize(); i++) {
            arguments.put(i, visit(ctx.funcRParams().param(i)));
        }
        if (funcSymbol.getRetType().equals(voidType)) {
            return LLVMBuildCall(builder, funcSymbol.getVal(), arguments, funcSymbol.getArgumentSize(), "");
        } else {
            return LLVMBuildCall(builder, funcSymbol.getVal(), arguments, funcSymbol.getArgumentSize(), "returnValue");
        }
    }

    @Override
    public LLVMValueRef visitExp_paren(SysYParser.Exp_parenContext ctx) {
        return visit(ctx.exp());
    }
}
