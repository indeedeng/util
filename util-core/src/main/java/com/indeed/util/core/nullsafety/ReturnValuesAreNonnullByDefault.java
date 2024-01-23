package com.indeed.util.core.nullsafety;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.checkerframework.framework.qual.TypeUseLocation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation for types or packages which warns if null is returned from a method not annotated with
 * {@link org.checkerframework.checker.nullness.qual.Nullable}
 *
 * @deprecated Use {@link NonnullByDefault}
 */
@Deprecated
@Documented
@DefaultQualifier(value = NonNull.class, locations = TypeUseLocation.RETURN)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReturnValuesAreNonnullByDefault {}
