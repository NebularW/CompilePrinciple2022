; ModuleID = 'module'
source_filename = "module"

@sort_arr = global [5 x i32] zeroinitializer

define i32 @f(i32* %0) {
fEntry:
  %pointer_arr1 = alloca i32*, align 8
  store i32* %0, i32** %pointer_arr1, align 8
  %arr1 = load i32*, i32** %pointer_arr1, align 8
  %arr11 = getelementptr i32, i32* %arr1, i32 0
  %arr112 = load i32, i32* %arr11, align 4
  ret i32 %arr112
}

define i32 @combine(i32* %0, i32 %1, i32* %2, i32 %3) {
combineEntry:
  %pointer_arr1 = alloca i32*, align 8
  store i32* %0, i32** %pointer_arr1, align 8
  %pointer_arr1_length = alloca i32, align 4
  store i32 %1, i32* %pointer_arr1_length, align 4
  %pointer_arr2 = alloca i32*, align 8
  store i32* %2, i32** %pointer_arr2, align 8
  %pointer_arr2_length = alloca i32, align 4
  store i32 %3, i32* %pointer_arr2_length, align 4
  %arr1 = load i32*, i32** %pointer_arr1, align 8
  %"<init>" = getelementptr i32, i32* %arr1, i32 0
  store i32 9, i32* %"<init>", align 4
  %pointer_arr = alloca [1 x i32], align 4
  %GEP_0 = getelementptr [1 x i32], [1 x i32]* %pointer_arr, i32 0, i32 0
  store i32 8, i32* %GEP_0, align 4
  %"arr[0]" = getelementptr [1 x i32], [1 x i32]* %pointer_arr, i32 0, i32 0
  %arr11 = load i32*, i32** %pointer_arr1, align 8
  %GEP_02 = getelementptr i32, i32* %arr11, i32 0
  %arr113 = load i32, i32* %GEP_02, align 4
  store i32 %arr113, i32* %"arr[0]", align 4
  %arr14 = load i32*, i32** %pointer_arr1, align 8
  %returnValue = call i32 @f(i32* %arr14)
  ret i32 %returnValue
}

define i32 @main() {
mainEntry:
  %pointer_a = alloca [2 x i32], align 4
  %GEP_0 = getelementptr [2 x i32], [2 x i32]* %pointer_a, i32 0, i32 0
  store i32 1, i32* %GEP_0, align 4
  %GEP_1 = getelementptr [2 x i32], [2 x i32]* %pointer_a, i32 0, i32 1
  store i32 5, i32* %GEP_1, align 4
  %pointer_b = alloca [3 x i32], align 4
  %GEP_01 = getelementptr [3 x i32], [3 x i32]* %pointer_b, i32 0, i32 0
  store i32 1, i32* %GEP_01, align 4
  %GEP_12 = getelementptr [3 x i32], [3 x i32]* %pointer_b, i32 0, i32 1
  store i32 4, i32* %GEP_12, align 4
  %GEP_2 = getelementptr [3 x i32], [3 x i32]* %pointer_b, i32 0, i32 2
  store i32 14, i32* %GEP_2, align 4
  %GEP_a = getelementptr [2 x i32], [2 x i32]* %pointer_a, i32 0, i32 0
  %GEP_b = getelementptr [3 x i32], [3 x i32]* %pointer_b, i32 0, i32 0
  %returnValue = call i32 @combine(i32* %GEP_a, i32 2, i32* %GEP_b, i32 3)
  ret i32 %returnValue
}
