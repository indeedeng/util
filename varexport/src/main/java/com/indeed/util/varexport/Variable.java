// Copyright 2009 Indeed
package com.indeed.util.varexport;


import java.io.PrintWriter;
import java.util.Set;
import java.util.Map;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableSet;
import com.google.common.escape.Escaper;

/**
 * Variable exported by {@link com.indeed.util.varexport.VarExporter}.
 *
 * @see com.indeed.util.varexport.Export
 * @author jack@indeed.com (Jack Humphrey)
 */
public abstract class Variable<T> {

    private static Set<Character> nameValueSpecialCharacters = ImmutableSet.of(':', '=');

    private String name;
    private String doc;
    private boolean expand;

    public Variable(String name, String doc, boolean expand) {
        this.name = name;
        this.doc = doc;
        this.expand = expand;
    }

    public String getName() {
        return name;
    }

    public String getDoc() {
        return doc != null ? doc : "";
    }

    /** @return epoch millis when value was last updated, null if value is current */
    public Long getLastUpdated() {
        return null;
    }

    public boolean isExpandable() {
        return expand && canExpand();
    }

    public Map<?, ?> expand() {
        if (!isExpandable()) {
            throw new RuntimeException(name + " is not expandable");
        }
        return (Map<?, ?>) getValue();
    }

    public boolean hasDoc() {
        return !Strings.isNullOrEmpty(getDoc());
    }

    protected abstract boolean canExpand();

    public abstract T getValue();

    /**
     * Write name and value (and optionally documentation) in {@link java.util.Properties} format.
     * @param out writer
     * @param includeDoc true if should be prefixed with documentation line
     */
    public void write(PrintWriter out, boolean includeDoc) {
        // getValue() will update the timestamp (if relevant)
        T localValue = getValue();
        if (includeDoc) {
            out.println();
            Long lastUpdate = getLastUpdated();
            if (hasDoc() || lastUpdate != null) {
                out.print("# ");
                if (hasDoc()) {
                    out.print(makePropertiesSafe(getDoc(), null));
                }
                if (lastUpdate != null) {
                    out.print(" (last update: " + lastUpdate + ")");
                }
                out.println();
            }
        }
        out.println(toString());
    }

    /**
     * Write value (escaped for {@link java.util.Properties} format).
     * @param out writer
     */
    public void writeValue(PrintWriter out) {
        out.print(makePropertiesSafe(getValue(), nameValueSpecialCharacters));
    }

    public String getValueString() {
        T value = getValue();
        return "" + value;
    }

    public String getSafeValue() {
        return makePropertiesSafe(getValue(), nameValueSpecialCharacters);
    }

    public String getSafeName() {
        return makePropertiesSafe(name, nameValueSpecialCharacters);
    }

    public String toString() {
        return getSafeName() + "=" + getSafeValue();
    }

    private static String makePropertiesSafe(Object in, Set<Character> toEscape) {
        if (in == null) { return "null"; }
        String str = in.toString();
        StringBuilder result = null;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            final boolean isAscii = CharMatcher.ASCII.matches(c);
            final boolean requiresEscape = !isAscii || (toEscape != null && toEscape.contains(c));
            if (requiresEscape && result == null) {
                result = new StringBuilder(str.length() * 2);
                result.append(str, 0, i);
            }
            if (!isAscii) {
                result.append(JavaSourceEscaper.escapeChar(c));
            } else {
                if (requiresEscape) {
                    result.append('\\');
                }
                if (result != null) {
                    result.append(c);
                }
            }
        }
        return result == null ? str : result.toString();
    }

    /** Escapes non-ASCII using \\unnnn format */
    private static class JavaSourceEscaper extends Escaper {
        // TODO: remove if guava ever adds SourceCodeEscapers
        public String escape(String input) {
            StringBuilder b = new StringBuilder();
            for (Character c : input.toCharArray()) {
                if (!CharMatcher.ASCII.matches(c)) {
                    b.append(escapeChar(c));
                } else {
                    b.append(c);
                }
            }
            return b.toString();
        }

        public static String escapeChar(char c) {
            if (c < 0x10) {
                return "\\u000" + Integer.toHexString(c);
            } else if (c < 0x100) {
                return "\\u00" + Integer.toHexString(c);
            } else if (c < 0x1000) {
                return "\\u0" + Integer.toHexString(c);
            }
            return "\\u" + Integer.toHexString(c);
        }
    }
}
