package com.indeed.util.serialization.splitter;

import com.google.common.base.CharMatcher;
import com.google.common.base.Supplier;
import com.google.common.collect.AbstractIterator;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import java.util.Iterator;

/**
 * @author jplaisance
 */
public final class EscapeAwareSplitter {
    private static final Logger log = Logger.getLogger(EscapeAwareSplitter.class);
    
    public static final Supplier<Lexer> NO_ESCAPE_LEXER_SUPPLIER = new Supplier<Lexer>() {
        @Override
        public Lexer get() {
            return new Lexer() {

                String str;
                CharMatcher delimiter;
                
                int startIndex = -1;
                int endIndex = 0;
                
                @Override
                public void setInput(String str) {
                    this.str = str;
                }

                @Override
                public void setDelimiter(CharMatcher delimiter) {
                    this.delimiter = delimiter;
                }

                @Override
                public boolean accept(int index) throws ParseException {
                    char c = str.charAt(index);
                    if (startIndex < 0) {
                        startIndex = index;
                    }
                    if (delimiter.matches(c)) {
                        return false;
                    }
                    endIndex = index+1;
                    return true;
                }

                @Override
                public void reset() {
                    startIndex = -1;
                    endIndex = 0;
                }

                @Override
                public String get() throws ParseException {
                    if (startIndex < 0) throw new IllegalStateException("this shouldn't happen");
                    return str.substring(startIndex, endIndex);
                }
            };
        }
    };

    public static final Supplier<Lexer> ESCAPE_JAVA_LEXER_SUPPLIER = new Supplier<Lexer>() {
        @Override
        public Lexer get() {

            return new Lexer() {

                String str;
                CharMatcher delimiter;
                StringBuilder builder = new StringBuilder();

                int startIndex = -1;
                int endIndex = 0;
                boolean quoted = false;
                boolean escaped = false;

                @Override
                public void setInput(String str) {
                    this.str = str;
                }

                @Override
                public void setDelimiter(CharMatcher delimiter) {
                    this.delimiter = delimiter;
                }

                @Override
                public boolean accept(final int index) throws ParseException {
                    char c = str.charAt(index);
                    if (startIndex < 0) {
                        startIndex = index;
                    }
                    if (escaped) {
                        escaped = false;
                    } else {
                        if (!quoted && delimiter.matches(c)) {
                            return false;
                        }
                        if (c == '\\') {
                            escaped = true;
                        }
                        if (c == '"') {
                            builder.append(StringEscapeUtils.unescapeJava(str.substring(startIndex, index)));
                            startIndex = index+1;
                            quoted = !quoted;
                        }
                    }
                    endIndex = index+1;
                    return true;
                }

                @Override
                public void reset() {
                    startIndex = -1;
                    endIndex = 0;
                    quoted = false;
                    escaped = false;
                    builder.setLength(0);
                }

                @Override
                public String get() throws ParseException {
                    if (quoted) throw new ParseException("quote not closed");
                    if (escaped) throw new ParseException("improper escape");
                    if (startIndex < 0) throw new IllegalStateException("this shouldn't happen");
                    builder.append(StringEscapeUtils.unescapeJava(str.substring(startIndex, endIndex)));
                    return builder.toString();
                }
            };
        }
    };

    private final CharMatcher delimiter;
    private final Supplier<Lexer> lexerSupplier;

    public EscapeAwareSplitter(CharMatcher delimiter, Supplier<Lexer> lexerSupplier) {
        this.delimiter = delimiter;
        this.lexerSupplier = lexerSupplier;
    }

    public Iterator<String> split(final String str) {
        return new AbstractIterator<String>() {

            int index = 0;
            
            Lexer lexer;
            
            {
                lexer = lexerSupplier.get();
                lexer.setInput(str);
                lexer.setDelimiter(delimiter);
            }
            
            @Override
            protected String computeNext() {
                if (index >= str.length()) {
                    endOfData();
                    return null;
                }
                try {
                    while (index < str.length() && delimiter.matches(str.charAt(index))) {
                        index++;
                    }
                    boolean matched = false;
                    while (index < str.length() && lexer.accept(index)) {
                        matched = true;
                        index++;
                    }
                    if (matched) {
                        String ret = lexer.get();
                        lexer.reset();
                        index++;
                        return ret;
                    }
                    endOfData();
                    return null;
                } catch (ParseException e) {
                    throw new IllegalStateException(e);
                }
            }
        };
    }
    
    public static interface Lexer {
        
        public void setInput(String str);

        public void setDelimiter(CharMatcher delimiter);

        public boolean accept(int index) throws ParseException;

        public void reset();
        
        public String get() throws ParseException;
    }

    public static final class ParseException extends Exception {
        public ParseException() {
        }

        public ParseException(String message) {
            super(message);
        }

        public ParseException(String message, Throwable cause) {
            super(message, cause);
        }

        public ParseException(Throwable cause) {
            super(cause);
        }
    }
}
