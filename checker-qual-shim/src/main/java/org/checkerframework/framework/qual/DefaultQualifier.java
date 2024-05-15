/**
 * Checker Framework qualifiers Copyright 2004-present by the Checker Framework developers
 *
 * <p>MIT License:
 *
 * <p>Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * <p>The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * <p>THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.checkerframework.framework.qual;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Applied to a declaration of a package, type, method, variable, etc., specifies that the given
 * annotation should be the default. The default is applied to type uses within the declaration for
 * which no other annotation is explicitly written. (The default is not applied to the "parametric
 * locations": class declarations, type parameter declarations, and type parameter uses.) If
 * multiple {@code DefaultQualifier} annotations are in scope, the innermost one takes precedence.
 * DefaultQualifier takes precedence over {@link DefaultQualifierInHierarchy}.
 *
 * <p>You may write multiple {@code @DefaultQualifier} annotations (for unrelated type systems, or
 * with different {@code locations} fields) at the same location. For example:
 *
 * <pre>
 * &nbsp; @DefaultQualifier(NonNull.class)
 * &nbsp; @DefaultQualifier(value = NonNull.class, locations = TypeUseLocation.IMPLICIT_UPPER_BOUND)
 * &nbsp; @DefaultQualifier(Tainted.class)
 * &nbsp; class MyClass { ... }
 * </pre>
 *
 * <p>This annotation currently has no effect in stub files.
 *
 * @see org.checkerframework.framework.qual.TypeUseLocation
 * @see DefaultQualifierInHierarchy
 * @see DefaultFor
 * @checker_framework.manual #defaults Default qualifier for unannotated types
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({
    ElementType.PACKAGE,
    ElementType.TYPE,
    ElementType.CONSTRUCTOR,
    ElementType.METHOD,
    ElementType.FIELD,
    ElementType.LOCAL_VARIABLE,
    ElementType.PARAMETER
})
@Repeatable(DefaultQualifier.List.class)
public @interface DefaultQualifier {

    /**
     * The Class for the default annotation.
     *
     * <p>To prevent affecting other type systems, always specify an annotation in your own type
     * hierarchy. (For example, do not set {@link
     * org.checkerframework.common.subtyping.qual.Unqualified} as the default.)
     */
    Class<? extends Annotation> value();

    /**
     * Returns the locations to which the annotation should be applied.
     *
     * @return the locations to which the annotation should be applied
     */
    TypeUseLocation[] locations() default {TypeUseLocation.ALL};

    /**
     * A wrapper annotation that makes the {@link DefaultQualifier} annotation repeatable.
     *
     * <p>Programmers generally do not need to write this. It is created by Java when a programmer
     * writes more than one {@link DefaultQualifier} annotation at the same location.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({
        ElementType.PACKAGE,
        ElementType.TYPE,
        ElementType.CONSTRUCTOR,
        ElementType.METHOD,
        ElementType.FIELD,
        ElementType.LOCAL_VARIABLE,
        ElementType.PARAMETER
    })
    public static @interface List {
        /**
         * Return the repeatable annotations.
         *
         * @return the repeatable annotations
         */
        DefaultQualifier[] value();
    }
}
