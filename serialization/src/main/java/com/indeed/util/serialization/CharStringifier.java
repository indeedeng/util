package com.indeed.util.serialization;

import org.apache.log4j.Logger;

/**
 * @author jplaisance
 */
public final class CharStringifier implements Stringifier<Character>{
    private static final Logger log = Logger.getLogger(CharStringifier.class);

    @Override
    public String toString(Character character) {
        return character.toString();
    }

    @Override
    public Character fromString(String str) {
        return str.charAt(0);
    }
}
