; ModuleID = 'module'
source_filename = "module"

@a = global i32 0
@b = global i32 0
@c = global i32 0

define i32 @main() {
mainEntry:
  store i32 1, i32* @a, align 4
  store i32 2, i32* @b, align 4
  store i32 3, i32* @c, align 4
  %a = load i32, i32* @a, align 4
  %b = load i32, i32* @b, align 4
  %LT = icmp slt i32 %a, %b
  %ext = zext i1 %LT to i32
  %c = load i32, i32* @c, align 4
  %LT1 = icmp slt i32 %ext, %c
  %ext2 = zext i1 %LT1 to i32
  %cmp_result = icmp ne i32 0, %ext2
  br i1 %cmp_result, label %true, label %false

true:                                             ; preds = %mainEntry
  store i32 5, i32* @c, align 4
  br label %entry

false:                                            ; preds = %mainEntry
  store i32 0, i32* @c, align 4
  br label %entry

entry:                                            ; preds = %false, %true
  %c3 = load i32, i32* @c, align 4
  ret i32 %c3
}
