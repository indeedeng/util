// Copyright 2009 Indeed
package com.indeed.util.varexport;


import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.escape.Escaper;

import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Variable exported by {@link com.indeed.util.varexport.VarExporter}.
 *
 * @see com.indeed.util.varexport.Export
 * @author jack@indeed.com (Jack Humphrey)
 */
public abstract class Variable<T> {

    private static Set<Character> nameValueSpecialCharacters = ImmutableSet.of(':', '=');

    private static Pattern INDEXABLE_SPLITTER = Pattern.compile("[^a-z0-9]+");

    private final String name;
    private final String[] indexableNames;
    private final String doc;
    private final String namespace;
    private final boolean expand;
    private final Set<String> tags;

    private static String[] buildIndexableName(final String name) {
        if (name == null) {
            return new String[0];
        }
        return INDEXABLE_SPLITTER.split(name.toLowerCase());
    }

    public Variable(String name, Set<String> tags, String doc, boolean expand, String namespace) {
        this.name = name;
        this.indexableNames = buildIndexableName(name);
        this.tags = tags;
        this.doc = doc;
        this.expand = expand;
        this.namespace = namespace;
    }

    public String getName() {
        return name;
    }

    /**
     * @deprecated do not use this.
     * Please implement your own tokenization of variable names, if needed.
     */
    @Deprecated
    public String[] getIndexableNames() {
        return indexableNames;
    }

    public String getDoc() {
        return doc != null ? doc : "";
    }

    public Set<String> getTags() {
        return tags;
    }

    public String getNamespace() { return namespace; }

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

    /**
     * @return True as long as the variable is "live" and should continue to be valid/usable
     */
    protected abstract boolean isLive();

    protected abstract boolean canExpand();

    public abstract T getValue();

    public String getNamespaceAndName() {
        if(Strings.isNullOrEmpty(namespace)) {
            return name;
        }

        return namespace + "-" + name;
    }

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
            final boolean isAscii = CharMatcher.ascii().matches(c);
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
            for (int i = 0; i < input.length(); i++) {
                char c = input.charAt(i);
                if (!CharMatcher.ascii().matches(c)) {
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
