package com.indeed.util.serialization;

import org.apache.log4j.Logger;

/**
 * @author jplaisance
 */
public final class ClassStringifier implements Stringifier<Class> {
    private static final Logger log = Logger.getLogger(ClassStringifier.class);

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
