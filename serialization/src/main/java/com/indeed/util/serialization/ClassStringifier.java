package com.indeed.util.serialization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author jplaisance */
public final class ClassStringifier implements Stringifier<Class> {
    private static final Logger log = LoggerFactory.getLogger(ClassStringifier.class);

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
