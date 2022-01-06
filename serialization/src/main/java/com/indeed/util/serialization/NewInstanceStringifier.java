package com.indeed.util.serialization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jplaisance
 */
public final class NewInstanceStringifier implements Stringifier<Object> {
    private static final Logger log = LoggerFactory.getLogger(NewInstanceStringifier.class);

    @Override
    public String toString(Object o) {
        return o.getClass().getName();
    }

    @Override
    public Object fromString(String str) {
        try {
            return Class.forName(str).newInstance();
        } catch (InstantiationException e) {
            throw new IllegalArgumentException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
