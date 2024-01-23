package com.indeed.util.core.nullsafety;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.checkerframework.framework.qual.TypeUseLocation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation for types or packages which assumes all parameters, member variables, and return
 * values are @Nonnull unless indicated otherwise with {@link
 * org.checkerframework.checker.nullness.qual.Nullable}
 */
@Documented
@DefaultQualifier(
        value = NonNull.class,
        locations = {TypeUseLocation.FIELD, TypeUseLocation.RETURN, TypeUseLocation.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NonnullByDefault {}
