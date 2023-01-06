; ModuleID = 'module'
source_filename = "module"

define i32 @f(i32 %0) {
fEntry:
  %pointer_i = alloca i32, align 4
  store i32 %0, i32* %pointer_i, align 4
  %i = load i32, i32* %pointer_i, align 4
  ret i32 %i
}

define void @g() {
gEntry:
  %pointer_j = alloca i32, align 4
  store i32 6, i32* %pointer_j, align 4
  ret void
}

define void @func() {
funcEntry:
  ret void
}

define i32 @main() {
mainEntry:
  %pointer_a = alloca i32, align 4
  store i32 1, i32* %pointer_a, align 4
  %pointer_array = alloca [5 x i32], align 4
  %GEP_0 = getelementptr [5 x i32], [5 x i32]* %pointer_array, i32 0, i32 0
  store i32 1, i32* %GEP_0, align 4
  %GEP_1 = getelementptr [5 x i32], [5 x i32]* %pointer_array, i32 0, i32 1
  store i32 2, i32* %GEP_1, align 4
  %GEP_2 = getelementptr [5 x i32], [5 x i32]* %pointer_array, i32 0, i32 2
  store i32 3, i32* %GEP_2, align 4
  %GEP_3 = getelementptr [5 x i32], [5 x i32]* %pointer_array, i32 0, i32 3
  store i32 0, i32* %GEP_3, align 4
  %GEP_4 = getelementptr [5 x i32], [5 x i32]* %pointer_array, i32 0, i32 4
  store i32 0, i32* %GEP_4, align 4
  %"array[3]" = getelementptr [5 x i32], [5 x i32]* %pointer_array, i32 0, i32 3
  %a = load i32, i32* %pointer_a, align 4
  store i32 %a, i32* %"array[3]", align 4
  %"array[4]" = getelementptr [5 x i32], [5 x i32]* %pointer_array, i32 0, i32 4
  %a1 = load i32, i32* %pointer_a, align 4
  %add_ = add i32 %a1, 5
  store i32 %add_, i32* %"array[4]", align 4
  call void @g()
  %a2 = load i32, i32* %pointer_a, align 4
  %"array[4]3" = getelementptr [5 x i32], [5 x i32]* %pointer_array, i32 0, i32 4
  %array1 = load i32, i32* %"array[4]3", align 4
  %mul_ = mul i32 %a2, %array1
  %returnValue = call i32 @f(i32 %mul_)
  ret i32 %returnValue
}
