package com.indeed.util.serialization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jplaisance
 */
public final class CharStringifier implements Stringifier<Character>{
    private static final Logger log = LoggerFactory.getLogger(CharStringifier.class);

    @Override
    public String toString(Character character) {
        return character.toString();
    }

    @Override
    public Character fromString(String str) {
        return str.charAt(0);
    }
}
