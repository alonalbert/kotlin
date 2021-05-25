// !USE_EXPERIMENTAL: kotlin.RequiresOptIn
// FILE: api.kt

package api

import kotlin.annotation.AnnotationTarget.*

@RequiresOptIn(level = RequiresOptIn.Level.WARNING)
@Target(CLASS, ANNOTATION_CLASS, TYPE_PARAMETER, PROPERTY, FIELD, LOCAL_VARIABLE, VALUE_PARAMETER, CONSTRUCTOR, FUNCTION,
        PROPERTY_SETTER, TYPE, TYPEALIAS)
@Retention(AnnotationRetention.BINARY)
annotation class E1

@RequiresOptIn(level = RequiresOptIn.Level.WARNING)
<!EXPERIMENTAL_ANNOTATION_WITH_WRONG_TARGET!>@Target(FILE)<!>
annotation <!EXPERIMENTAL_ANNOTATION_WITH_WRONG_RETENTION_WARNING!>class E2<!>

@RequiresOptIn(level = RequiresOptIn.Level.WARNING)
<!EXPERIMENTAL_ANNOTATION_WITH_WRONG_TARGET!>@Target(EXPRESSION)<!>
<!EXPERIMENTAL_ANNOTATION_WITH_WRONG_RETENTION!>@Retention(AnnotationRetention.SOURCE)<!>
annotation class E3

@RequiresOptIn(level = RequiresOptIn.Level.WARNING)
@Target(PROPERTY_GETTER)
@Retention(AnnotationRetention.BINARY)
annotation class E4

@RequiresOptIn(level = RequiresOptIn.Level.WARNING)
@Target(PROPERTY_SETTER)
@Retention(AnnotationRetention.BINARY)
annotation class E5

@RequiresOptIn(level = RequiresOptIn.Level.WARNING)
@Target(PROPERTY, FUNCTION, PROPERTY_SETTER)
@Retention(AnnotationRetention.BINARY)
annotation class E6

var some: Int
    <!EXPERIMENTAL_ANNOTATION_ON_GETTER!>@E4<!>
    get() = 42
    @E5
    set(value) {}

class My {
    <!EXPERIMENTAL_ANNOTATION_ON_OVERRIDE!>@E6<!>
    override fun hashCode() = 0
}

interface Base {
    val bar: Int

    val baz: Int

    @E6
    fun foo()
}

class Derived : Base {
    <!EXPERIMENTAL_ANNOTATION_ON_OVERRIDE!>@E6<!>
    override val bar: Int = 42

    @set:E6
    override var baz: Int = 13

    @E6
    override fun foo() {}
}