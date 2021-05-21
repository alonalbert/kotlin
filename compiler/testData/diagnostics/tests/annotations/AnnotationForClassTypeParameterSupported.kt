// !LANGUAGE: +ClassTypeAnnotations
annotation class A1
annotation class A2(val some: Int = 12)

class TopLevelClass<@A1 @A2(3) @A2 @A1(<!TOO_MANY_ARGUMENTS!>12<!>) @A2(<!TYPE_MISMATCH!>"Test"<!>) T> {
    class InnerClass<@A1 @A2(3) @A2 @A1(<!TOO_MANY_ARGUMENTS!>12<!>) @A2(<!TYPE_MISMATCH!>"Test"<!>) T> {
        fun test() {
            class InFun<@<!DEBUG_INFO_MISSING_UNRESOLVED!>A1<!> @<!DEBUG_INFO_MISSING_UNRESOLVED!>A2<!>(3) @<!DEBUG_INFO_MISSING_UNRESOLVED!>A2<!> @<!DEBUG_INFO_MISSING_UNRESOLVED!>A1<!>(12) @<!DEBUG_INFO_MISSING_UNRESOLVED!>A2<!>("Test") T>
        }
    }
}
