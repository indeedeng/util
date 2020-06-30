package com.indeed.util.serialization;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * @author jplaisance
 */
public final class ClassStringifier implements Stringifier<Class> {
    private static final Logger log = LogManager.getLogger(ClassStringifier.class);

    @Override
    public String toString(Class aClass) {
        return aClass.getName();
    }

    @Override
    public Class fromString(String str) {
        try {
            return Class.forName(str);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
