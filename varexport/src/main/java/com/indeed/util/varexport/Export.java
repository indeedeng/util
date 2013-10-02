// Copyright 2009 Indeed
package com.indeed.util.varexport;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation for exporting variables for use with VarExporter.
 *
 * @author jack@indeed.com (Jack Humphrey)
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Export {
    /** @return Name to use for variable -- default will result in use of field/method name */
    public String name() default "";

    /** @return Documentation about the variable */
    public String doc() default "";

    /** @return true if variables implementing {@link java.util.Map} should be expanded into multiple variables */
    public boolean expand() default false;

    /** @return how long (in milliseconds) to keep a value cached; default behavior for timeout 0 is no caching */
    public long cacheTimeoutMs() default 0L;
}
