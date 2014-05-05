package com.indeed.util.serialization;

/**
* @author jplaisance
*/
public class IntStringifier implements Stringifier<Integer> {

    @Override
    public String toString(final Integer integer) {
        return integer.toString();
    }

    @Override
    public Integer fromString(final String str) {
        return Integer.valueOf(str);
    }
}
