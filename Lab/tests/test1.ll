; ModuleID = 'module'
source_filename = "module"

define i32 @main() {
mainEntry:
  %pointer_a = alloca i32, align 4
  store i32 0, i32* %pointer_a, align 4
  %pointer_count = alloca i32, align 4
  store i32 0, i32* %pointer_count, align 4
  br label %whileCondition

whileCondition:                                   ; preds = %entry8, %true, %mainEntry
  %a = load i32, i32* %pointer_a, align 4
  %LE = icmp sle i32 %a, 0
  %ext = zext i1 %LE to i32
  %count = load i32, i32* %pointer_count, align 4
  %EQ = icmp eq i32 %count, 10
  %ext1 = zext i1 %EQ to i32
  %OR = icmp unknown i32 %ext, %ext1
  %ext2 = zext i1 %OR to i32
  %cmp_result = icmp ne i32 0, %ext2
  br i1 %cmp_result, label %whileBody, label %entry

whileBody:                                        ; preds = %whileCondition
  %a3 = load i32, i32* %pointer_a, align 4
  %sub_ = sub i32 %a3, 1
  store i32 %sub_, i32* %pointer_a, align 4
  %count4 = load i32, i32* %pointer_count, align 4
  %add_ = add i32 %count4, 1
  store i32 %add_, i32* %pointer_count, align 4
  %a5 = load i32, i32* %pointer_a, align 4
  %LT = icmp slt i32 %a5, -20
  %ext6 = zext i1 %LT to i32
  %cmp_result7 = icmp ne i32 0, %ext6
  br i1 %cmp_result7, label %true, label %false

entry:                                            ; preds = %whileCondition
  %count9 = load i32, i32* %pointer_count, align 4
  ret i32 %count9

true:                                             ; preds = %whileBody
  br label %whileCondition
  br label %entry8

false:                                            ; preds = %whileBody
  br label %entry8

entry8:                                           ; preds = %false, %true
  br label %whileCondition
}
