package com.indeed.util.core.nullsafety;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.checkerframework.framework.qual.TypeUseLocation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation for types or packages which assumes all member variables are @Nonnull unless indicated
 * otherwise with {@link org.checkerframework.checker.nullness.qual.Nullable}
 *
 * @deprecated Use {@link NonnullByDefault}
 */
@Deprecated
@Documented
@DefaultQualifier(value = NonNull.class, locations = TypeUseLocation.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldsAreNonnullByDefault {}
