package com.indeed.util.varexport;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author jack@indeed.com (Jack Humphrey)
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface LegacyExport {
    public String name() default "";
    public String doc() default "";
    public boolean expand() default false;
    public long cacheTimeoutMs() default 0L;
}
