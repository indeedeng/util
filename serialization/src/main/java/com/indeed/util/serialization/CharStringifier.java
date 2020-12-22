package com.indeed.util.serialization;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * @author jplaisance
 */
public final class CharStringifier implements Stringifier<Character>{
    private static final Logger log = LogManager.getLogger(CharStringifier.class);

    @Override
    public String toString(Character character) {
        return character.toString();
    }

    @Override
    public Character fromString(String str) {
        return str.charAt(0);
    }
}
